package org.seckill.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.seckill.BaseTest;
import org.seckill.entity.SuccessKilled;

public class SuccessKilledDaoTest extends BaseTest {

	@Resource
	private SuccessKilledDao successKilledDao;

	@Test
	public void testInsertSuccessKilled() throws Exception {
		/**
		 * 第一次执行: Parameters: 1000(Long), 13631231234(Long) ===> insertCount=1
		 * 第二次执行(id=1000的这个产品被phone=13631231234的这个人第二次秒杀): 
		 * 			Parameters: 1000(Long), 13631231234(Long) ===> insertCount=0 //说明同一个user不允许重复秒杀(不允许重复insert购买成功的记录)！
		 */
		long id = 1000L;
		long phone = 13631231234L;
		int insertCount = successKilledDao.insertSuccessKilled(id, phone);
		System.out.println("insertCount=" + insertCount);
	}

	@Test
	public void testQueryByIdWithSeckill() throws Exception {
		long id = 1000L;
		long phone = 13631231234L;
		SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id, phone);
		System.out.println(successKilled);
		System.out.println(successKilled.getSeckill());
	}

}
