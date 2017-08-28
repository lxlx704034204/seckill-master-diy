package org.seckill.service.impl;

import java.util.List;

import org.junit.Test;
import org.seckill.BaseTest;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SeckillServiceImplTest extends BaseTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// slf4j的实现 是logback.xml 的配置!!!
	//logback.qos.ch/manual/cofiguration.html

	@Autowired
	private SeckillService seckillService;

	@Test
	public void testGetSeckillList() throws Exception {
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}", list); // 这里的"{}"是占位符 ，打印的时候 list的实际字串 会替换它
	}

	@Test
	public void testGetById() throws Exception {
		long id = 1000;
		Seckill seckill = seckillService.getById(id);
		logger.info("seckill={}", seckill);
		//seckill=Seckill [seckillId=1000, name=1000元秒杀iphone6, number=100, 
		//startTime=Wed Nov 01 00:00:00 GMT+08:00 2017, endTime=Thu Nov 02 00:00:00 GMT+08:00 2017, 
		//createTime=Wed Apr 26 00:25:23 GMT+08:00 2017]
	}

	@Test	//diy
	public void testExportSeckillUrl() throws Exception {
		long id = 1001;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		logger.info("exposer={}", exposer); //打印Exposer实体的   toString()方法
//		Exposer [exposed=true, 
//				md5=35927ce4449ce0f6600683f1b75e6b47, 
//				seckillId=1001, now=0, start=0, end=0]
	}
	
//	@Test //diy  这个test方法 没有 把所有编译期异常(或者检查型异常) 转换为运行期异常，so会导致 运行的时候 不通过，so把他注释掉！
//	public void testExecuteSeckill() throws Exception {
//		long id = 1001; long phone = 13631231230L; //设定test值的时候  要注意这两个联合主键的 约束
//		Exposer exposer = seckillService.exportSeckillUrl(id);
//		String md5 = exposer.getMd5();
//		SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
//		logger.info("execution={}", execution); //
///*																注册了一个                                                 同步的                                       SqlSession
//00:56:06.930 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Registering transaction synchronization for SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455]
//00:56:06.935 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@1e5855af] will be managed by Spring 被spring管理
//00:56:06.940 [main] DEBUG o.s.d.S.insertSuccessKilled - ==>  Preparing: INSERT ignore INTO success_killed (seckill_id, user_phone, state) VALUES (?, ?, 0) 
//00:56:06.966 [main] DEBUG o.s.d.S.insertSuccessKilled - ==> Parameters: 1001(Long), 13631231230(Long)
//00:56:06.966 [main] DEBUG o.s.d.S.insertSuccessKilled - <==    Updates: 1
//00:56:06.972 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455]
//00:56:06.973 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Fetched SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455] from current transaction
//00:56:06.973 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==>  Preparing: UPDATE seckill SET number = number - 1 WHERE seckill_id = ? AND start_time <= ? AND end_time >= ? AND number > 0 
//00:56:06.974 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==> Parameters: 1001(Long), 2017-04-28 00:56:06.92(Timestamp), 2017-04-28 00:56:06.92(Timestamp)
//00:56:06.974 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - <==    Updates: 1
//00:56:06.974 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455]
//00:56:06.975 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Fetched SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455] from current transaction
//00:56:06.977 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==>  Preparing: SELECT sk.seckill_id, sk.user_phone, sk.create_time, sk.state, s.seckill_id as "seckill.seckill_id", s.`name` "seckill.name", s.number "seckill.number", s.start_time "seckill.start_time", s.end_time "seckill.end_time", s.create_time "seckill.create_time" FROM success_killed sk INNER JOIN seckill s ON sk.seckill_id = s.seckill_id WHERE sk.seckill_id = ? AND sk.user_phone = ? 
//00:56:06.977 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==> Parameters: 1001(Long), 13631231230(Long)
//00:56:06.993 [main] DEBUG o.s.d.S.queryByIdWithSeckill - <==      Total: 1
//00:56:06.998 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455]
//00:56:06.998 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization committing SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455]
//00:56:06.998 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization deregistering SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455]
//00:56:06.999 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization closing SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@1f502455]
//																		SeckillExecution的 toString()方法
//00:56:07.004 [main] INFO  o.s.s.impl.SeckillServiceImplTest - 
//execution=SeckillExecution [seckillId=1001, state=1, stateInfo=秒杀成功, 
//							successKilled=SuccessKilled [seckillId=1001, userPhone=13631231230, state=0, creteTime=null]
//						   ]
//				
//    如果接着 你有执行该test方法，即：你成功秒杀了之后 又去秒杀(重复秒杀)，则就永远不会成功了(Updates 永远= 0)																		
//*/
//	
//	}
	
	// 注意集成测试业务涵盖的完整性： 测试代码完整逻辑，注意可重复执行
	@Test
	public void testSeckillLogic() throws Exception {
		long id = 1001;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		if (exposer.isExposed()) { //if秒杀 允许开始（已经开始），则打印秒杀信息
			logger.info("exposer={}", exposer);
			//exposer=Exposer [exposed=false, md5=null, seckillId=1001, now=1493226178665, start=1509465600000, end=1509552000000]
			long phone = 13631231234L;
			String md5 = exposer.getMd5();
			try {
				SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
				logger.info("execution={}", execution);
			} catch (RepeatKillException e) { 		//捕获 重复秒杀
				logger.error(e.getMessage());
			} catch (SeckillCloseException e) {		//捕获 SeckillClose 
				logger.error(e.getMessage());
			}
		} else { //if秒杀未开启，则打印 秒杀未开启 的警告(warn)信息
			// 秒杀未开启
			logger.warn("exposer={}", exposer);
		}
	}

	@Test
	public void testExecuteSeckillProcedure() throws Exception {
		long seckillId = 1000;
		long phone = 13631231234L;
		Exposer exposer = seckillService.exportSeckillUrl(seckillId);
		if (exposer.isExposed()) {
			String md5 = exposer.getMd5();
			SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
			logger.info(execution.getStateInfo());
		}
	}

}
