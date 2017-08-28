package org.seckill.service;

import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

/**
 * 业务接口：站在"使用者"角度设计接口 
 * 三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 */
public interface SeckillService {

	 //查询所有秒杀记录
	List<Seckill> getSeckillList();

	 //查询单个秒杀记录
	Seckill getById(long seckillId);

	//当秒杀开启的时候才输出 秒杀interface地址，否则输出系统time和秒杀time
	Exposer exportSeckillUrl(long seckillId);//是否暴露 秒杀接口地址(根据条件设置 是否开启秒杀 的方法：)

	/**
	 * // 执行秒杀操作 的方法
	 * 
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 * @return
	 * @throws SeckillException
	 * @throws RepeatKillException
	 * @throws SeckillCloseException
	 */
	SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException;

	/**
	 * 执行秒杀操作by存储过程
	 * 
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 * @return
	 * @throws SeckillException
	 * @throws RepeatKillException
	 * @throws SeckillCloseException
	 */
	SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException;

}
