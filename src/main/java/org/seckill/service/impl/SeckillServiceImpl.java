package org.seckill.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

//@Componet(所有的组件的注解(范畴比较大<模糊>，如果知道具体的则最好用具体的注解(eg:@Service @Dao @Controller)):); @Service; @Dao; @Controller;
@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// slf4j的实现 是logback.xml 的配置!!!
	//logback.qos.ch/manual/cofiguration.html

	// 注入Service依赖 @Autowired @Resource @Inject
	@Autowired
	private SeckillDao seckillDao;

	@Autowired //mybatis会implements所有Dao层接口，然后利用spring进行初始化 并 利用用‘@Autowired’注解额  来进行注入
	private SuccessKilledDao successKilledDao;

	@Autowired
	private RedisDao redisDao;
 
	
	@Override
	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	@Override
	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}
 
	//当秒杀开启的时候才输出 秒杀interface地址，否则输出系统time和秒杀time
	@Override //是否暴露 秒杀接口地址(根据条件设置 是否开启秒杀 的方法：)
	public Exposer exportSeckillUrl(long seckillId) {
		logger.info("-测试-0-：");
		
		// 优化点：缓存优化：超时的基础上维护一致性 
		Seckill seckill = redisDao.getSeckill(seckillId);// 1.访问Dao
		if (seckill == null) {
			logger.info("-测试-0.1-：");
			seckill = seckillDao.queryById(seckillId);	 // 2.访问数据库
			if (seckill == null) { // seckill == null 代表秒杀未开启(用exposed=false来记录)
				logger.info("-测试-0.2-：");
				return new Exposer(false, seckillId);
			} else {
				logger.info("-测试-0.3-：");
				redisDao.putSeckill(seckill);			 // 3.访问redis
			}
		}
		logger.info("-测试-1-：");
		if (seckill == null) {
			logger.info("-测试-1.2-：");
			return new Exposer(false, seckillId);
		}
		
		Date startTime = seckill.getStartTime(); //seckillId对应的商品的 秒杀开始time
		Date endTime = seckill.getEndTime();	 //seckillId对应的商品的 秒杀结束time	 
		Date nowTime = new Date();				 //系统当前时间
		
		logger.info("-测试-2.1-："+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime));
		logger.info("-测试-2.2-："+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTime));
		logger.info("-测试-2.3-："+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime));
		
		if ( nowTime.getTime() < startTime.getTime() 			//代表：秒杀还没开始
		  || nowTime.getTime() > endTime.getTime()) {		//代表：秒杀已经结束 	
			//  则    说明秒杀false(用exposed=false来记录,同时记录下后面三个信息)
			
			logger.info("-测试-2.4-：");
			
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		logger.info("-测试-3-：");
		
		// 转化特定字符串的过程，不可逆
		String md5 = getMD5(seckillId);
		logger.info("-测试-9-打印md5："+ md5); //35927ce4449ce0f6600683f1b75e6b47
		
		return new Exposer(true, md5, seckillId);//过滤了所有 exposed=false的情况以后，其余的所有情况下exposed都是=true了
	}
	private String getMD5(long seckillId) {
		String base = seckillId + "/" + slat; // 用MD5 混淆 keyId
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());//DigestUtils: spring专门应用MD5的类
		return md5;
	}
	// 设置混淆常量：md5盐值字符串，用于混淆MD5(我们不希望用户去猜到我们的 keyId)
	private final String slat = "skdfjksjdf7787%^%^%^FSKJFK*(&&%^%&^8DF8^%^^*7hFJDHFJ";//随便乱写，越复杂越好！

	
	
	@Override
	@Transactional //该Spring注解使得 进入和退出该方法的时候，自动加入事务control的逻辑
	/**
	 * 使用注解控制事务方法的优点： 1.开发团队达成一致约定，明确标注事务方法的编程风格
	 * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作，RPC/HTTP请求或者剥离到事务方法外部
	 * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		if (md5 == null || !md5.equals(getMD5(seckillId))) { 	//if用户传入的MD5=null 或者  (混淆常量 异常 || 改动seckillId了)
			throw new SeckillException("seckill data rewrite");	//则dialog 重写md5
		}
		// 执行秒杀逻辑： 记录购买行为 + 减库存
		Date now = new Date();
		
//		try {
//			int updateCount = seckillDao.reduceNumber(seckillId, now);// 减库存，热点商品竞争
//			if (updateCount <= 0) { // 没有更新到记录 则 说明秒杀已结束或秒杀未开始或者库存没有了，则 rollback 秒杀结束
//				throw new SeckillCloseException("seckill is closed");
//			} else {
//				int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);// 记录购买行为
//				if (insertCount <= 0) {// 重复秒杀(keyId冲突了)
//					throw new RepeatKillException("seckill repeated");
//				} else {
//					// 秒杀成功 commit
//					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
//				  //return new SeckillExecution(seckillId, 1, "秒杀成功", successKilled);//此构造方法不推荐
//					//				优化：		           类似            1, "秒杀成功"这样的常量 我们应该用 枚举，构成数据字典！！！
//					return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);//推荐 此改造后的构造方法
//				}
//			}
//		} catch (SeckillCloseException e1) {
//			throw e1;
//		} catch (RepeatKillException e2) {
//			throw e2;	
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			// 把所有编译期异常(或者检查型异常) 转换为运行期异常，这样spring会帮我们 rollback！(为了减库存的操作 与  购买行为的操作 不同时执行！)：
//			throw new SeckillException("seckill inner error:" + e.getMessage());//throw 业务异常
//		}
		 
		
		try {
 			int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);// 记录购买行为
 			//								   insert的时候可能会超时、db断开 等等，so需要加上try...catch...
 			
			// 唯一：seckillId,userPhone 联合主键
			if (insertCount <= 0) {// 重复秒杀(keyId冲突了)
				throw new RepeatKillException("seckill repeated");
			} else {
				int updateCount = seckillDao.reduceNumber(seckillId, now);// 减库存，热点商品竞争
				if (updateCount <= 0) { // 没有更新到记录 则 说明秒杀已结束或秒杀未开始或者库存没有了，则 rollback 秒杀结束
					throw new SeckillCloseException("seckill is closed");
				} else {
					// 秒杀成功 commit
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
				}
			}
		} catch (SeckillCloseException e1) { //抛出此异常 说明 秒杀关闭
			throw e1;
		} catch (RepeatKillException e2) {	//抛出此异常 说明 重复秒杀
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// 把所有编译期异常(或者检查型异常) 转换为运行期异常，这样spring会帮我们 rollback！(为了减库存的操作 与  购买行为的操作 不同时执行！)：
			throw new SeckillException("seckill inner error:" + e.getMessage());//throw 业务异常
		}
	}

	// 存储过程调用
	@Override
	public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
		if (md5 == null || !md5.equals(getMD5(seckillId))) {
			return new SeckillExecution(seckillId, SeckillStateEnum.DATA_REWRITE);
		}
		Date killTime = new Date();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("seckillId", seckillId);
		map.put("phone", userPhone);
		map.put("killTime", killTime);
		map.put("result", null);		// 接收  OUT r_result INT
		try {
			seckillDao.killByProcedure(map); // 执行存储过程，result被赋值
			// 获取result
			int result = MapUtils.getInteger(map, "result", -2);
			if (result == 1) { //代表通过调用存储过程  秒杀成功
				SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone); //在db中找到 秒杀执行成功的那个 对象 
				return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, sk);
			} else {
				return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
		}
	}

}
