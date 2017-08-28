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

@Controller // Ŀ�ģ��ѵ�ǰcontroller ����springMVC��������
@RequestMapping("/seckill") //���ô�controllerģ��С�seckill���� url:/ģ��/��Դ/{id}/ϸ�� /seckill/list
public class SeckillController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SeckillService seckillService;
	
 /*	Restful �� Struts ��ͬһ�� ���
	Restful: ��Դ״̬(����)��ת����post�� post /seckill/{seckillId}execution�����ǣ�post /seckill/execution/{seckillId}��
	 Get : ���ڡ���ѯ������  ����(�����ظ�ִ��)
	 Post: ���ڡ�add/�޸ġ� ���������ݵĲ�����
	 Put �� ���� ���޸ġ�	         ������    �ݵĲ�����
	 DELETE:  ��ɾ����	         ����
	 
	 url���:		/ģ��/��Դ/{��ʶ}/����1/...
	 	eg :	/user/{uid}/frends  ---> �����б�
	 
	 
*/	// http://localhost:8080/seckill/seckill/list
	@RequestMapping(value = "/list", method = RequestMethod.GET)//���������������ʽ��http���� ��ȫ���� ����
	public String list(Model model) { //������е���Ⱦ��list.jsp��������
		// ��ȡ�б�ҳ
		List<Seckill> list = seckillService.getSeckillList();
		model.addAttribute("list", list); // ������������յ�Ŀ���� ȡ��Ϊ��list�����������ȡ�ã�
		// list.jsp + model = spring���еģ�ModelAndView
		return "list";// WEB-INF/jsp/ "list" 	.jsp
					  //	"prefix"  "return"	"suffix" ��spring-web.xml�еģ�
	}

	// http://localhost:8080/seckill/seckill/detail?seckillId=1000L	//struts  ��ʽ·�����Ѳ��� д����diy��������	
	// http://localhost:8080/seckill/seckill/1000/detail			//Restful ��ʽ·�����Ѳ��� д����diy������ǰ��	
	@RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
	public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
		//			          ��@PathVariable ��url�е�ռλ����{seckillId}��ָ�� ʵ�Σ�  Ҳ���Բ�д���@PathVariable
		
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

	//�Ƿ�¶��ɱ   ajax����   ���� json
	@RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.POST, 
					produces = {"application/json; charset=utf-8" })
	@ResponseBody //�ô�ע�� ��Լ������json��ʽ�Ľ��
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

	//ִ����ɱ
	@RequestMapping(value = "/{seckillId}/{md5}/execution", method = RequestMethod.POST, produces = {"application/json; charset=utf-8" })
	@ResponseBody
	public SeckillResult<SeckillExecution> execute(
			@PathVariable("seckillId") Long seckillId,
			@PathVariable("md5") String md5, 
			@CookieValue(value = "killPhone", required = false) Long phone) //���û��� �������request�����е�cookie�л�ȡ���ģ��á�required = false������ע�����Ǳ����
	{
		// springmvc valid��֤��Ϣ
		if (phone == null) { //if Cookie����û�� phone
			return new SeckillResult<>(false, "δע��");
		}
		try {
			// �洢���̵���
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
