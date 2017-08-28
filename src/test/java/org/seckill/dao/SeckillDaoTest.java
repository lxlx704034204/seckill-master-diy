package org.seckill.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;
import org.junit.Test;
import org.seckill.BaseTest;
import org.seckill.entity.Seckill;

public class SeckillDaoTest extends BaseTest {
 
	@Resource //利用spring的 ‘@Resource’ 把SeckillDao接口的 实现类 实例对象 注入到当前类中：
	private SeckillDao seckillDao;
	
	@Test
	public void testQueryById() throws Exception {
		long id = 1000;//建表的时候已经设定了keyId从1000开始！
		Seckill seckill = seckillDao.queryById(id);
		System.out.println("-测试-1-："+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(seckill.getStartTime()));
		System.out.println("-测试-2-："+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(seckill.getEndTime()));
		System.out.println(seckill);
//		1000元秒杀iphone6
//		Seckill [seckillId=1000, name=1000元秒杀iphone6, number=100, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Thu Apr 20 22:50:09 GMT+08:00 2017]
 	}

	@Test
	public void testQueryAll() throws Exception  {
		//Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
		// java不能保存形参: 即：queryAll(int offset, int limit) --->java直接会自动处理成这样的---> queryAll(arg1,arg2)
		//当有多个参数的时候,你需要告诉mybatis哪个位置的参数(实参),它的key(形参)是什么！
		// so就需要这样的处理：在SeckillDao.java中的queryAll(...)方法中用‘@Param’ 告诉sql中的 哪个占位符 对应 user所传入的哪个参数！
		List<Seckill> seckills = seckillDao.queryAll(0, 100);//其实当前只insert了4条！
		for (Seckill seckill : seckills) {
			System.out.println(seckill);
		}
//		Seckill [seckillId=1000, name=1000元秒杀iphone6, 	number=100, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Thu Apr 20 22:50:09 GMT+08:00 2017]
//		Seckill [seckillId=1001, name=500元秒杀ipad2, 	number=200, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Thu Apr 20 22:50:09 GMT+08:00 2017]
//		Seckill [seckillId=1002, name=300元秒杀小米4, 		number=300, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Thu Apr 20 22:50:09 GMT+08:00 2017]
//		Seckill [seckillId=1003, name=200元秒杀红米note, 	number=400, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Thu Apr 20 22:50:09 GMT+08:00 2017]
	}

	@Test
	public void testReduceNumber() throws Exception {
		Date killTime = new Date();
		int updateCount = seckillDao.reduceNumber(1000L, killTime);
		System.out.println("updateCount=" + updateCount);//0代表失败
/*
23:40:39.927 [main] DEBUG org.mybatis.spring.SqlSessionUtils - SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@62d6b944] was not registered for synchronization because synchronization is not active
23:40:39.927 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@5df614ad] will not be managed by Spring
jdbc链接对象没有被spring所托管，而是从c3p0连接池拿到的  《===》	从c3p0连接池拿到jdbc链接对象 然后由spring所托管

23:40:39.927 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==>  Preparing: UPDATE seckill SET number = number - 1 WHERE seckill_id = ? AND start_time <= ? AND end_time >= ? AND number > 0 
23:40:39.928 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==> Parameters: 1000(Long), 2017-04-25 23:40:39.926(Timestamp), 2017-04-25 23:40:39.926(Timestamp)
23:40:39.968 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - <==    Updates: 0 //秒杀日期还没到or已经过期
*/
	}

}
