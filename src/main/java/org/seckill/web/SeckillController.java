package org.seckill.web;

import java.util.Date;
import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // 目的：把当前controller 放入springMVC容器当中
@RequestMapping("/seckill") //设置此controller模块叫‘seckill’； url:/模块/资源/{id}/细分 /seckill/list
public class SeckillController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SeckillService seckillService;
	
 /*	Restful 与 Struts 是同一类 框架
	Restful: 资源状态(名词)的转移用post： post /seckill/{seckillId}execution（而非：post /seckill/execution/{seckillId}）
	 Get : 用于“查询操作”  操作(可以重复执行)
	 Post: 用于“add/修改” 操作（非幂的操作）
	 Put ： 用于 “修改”	         操作（    幂的操作）
	 DELETE:  “删除”	         操作
	 
	 url设计:		/模块/资源/{标识}/集合1/...
	 	eg :	/user/{uid}/frends  ---> 好友列表
	 
	 
*/	// http://localhost:8080/seckill/seckill/list
	@RequestMapping(value = "/list", method = RequestMethod.GET)//不满足我们这个方式的http请求 将全部被 驳回
	public String list(Model model) { //存放所有的渲染“list.jsp”的数据
		// 获取列表页
		List<Seckill> list = seckillService.getSeckillList();
		model.addAttribute("list", list); // 给这个方法最终的目标结果 取名为“list”，方便别人取用！
		// list.jsp + model = spring当中的：ModelAndView
		return "list";// WEB-INF/jsp/ "list" 	.jsp
					  //	"prefix"  "return"	"suffix" （spring-web.xml中的）
	}

	// http://localhost:8080/seckill/seckill/detail?seckillId=1000L	//struts  格式路径（把参数 写在了diy方法名后）	
	// http://localhost:8080/seckill/seckill/1000/detail			//Restful 格式路径（把参数 写在了diy方法名前）	
	@RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
	public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
		//			          用@PathVariable 对url中的占位符“{seckillId}”指出 实参，  也可以不写这个@PathVariable
		
		if (seckillId == null) {
			return "redirect:/seckill/list";
		}
		Seckill seckill = seckillService.getById(seckillId);
		if (seckill == null) {
			return "forward:/seckill/list";
		}
		model.addAttribute("seckill", seckill);
		return "detail";
	}

	//是否暴露秒杀   ajax请求   返回 json
	@RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.POST, 
					produces = {"application/json; charset=utf-8" })
	@ResponseBody //用此注解 来约定返回json格式的结果
	public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
		SeckillResult<Exposer> result;
		try {
			Exposer exposer = seckillService.exportSeckillUrl(seckillId);
			result = new SeckillResult<Exposer>(true, exposer);
		} catch (Exception e) { 
			logger.error(e.getMessage(), e);
			result = new SeckillResult<Exposer>(false, e.getMessage());
		}
		return result;
	}

	//执行秒杀
	@RequestMapping(value = "/{seckillId}/{md5}/execution", method = RequestMethod.POST, produces = {"application/json; charset=utf-8" })
	@ResponseBody
	public SeckillResult<SeckillExecution> execute(
			@PathVariable("seckillId") Long seckillId,
			@PathVariable("md5") String md5, 
			@CookieValue(value = "killPhone", required = false) Long phone) //从用户的 浏览器的request请求中的cookie中获取到的，用‘required = false’来标注它不是必须的
	{
		// springmvc valid验证信息
		if (phone == null) { //if Cookie里面没有 phone
			return new SeckillResult<>(false, "未注册");
		}
		try {
			// 存储过程调用
			SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
			return new SeckillResult<SeckillExecution>(true, execution);
		} catch (RepeatKillException e) {
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
			return new SeckillResult<SeckillExecution>(true, execution);
		} catch (SeckillCloseException e) { 
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
			return new SeckillResult<SeckillExecution>(true, execution);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
			return new SeckillResult<SeckillExecution>(true, execution);
		}
	}

	// http://localhost:8080/seckill/list/time/now
	@RequestMapping(value = "/time/now", method = RequestMethod.GET)
	@ResponseBody
	public SeckillResult<Long> time() {
		Date now = new Date();
		return new SeckillResult<Long>(true, now.getTime());
	}

}
