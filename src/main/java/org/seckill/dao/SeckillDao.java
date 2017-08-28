package org.seckill.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

/**
 * 秒杀库存DAO接口
 * eclipse创建Dao层的Test类的方法：http://jingyan.baidu.com/article/ab69b270d63f572ca6189f51.html
 * @author 李奕锋
 */
public interface SeckillDao {

	/**
	 * 减库存
	 * 
	 * @param seckillId
	 * @param killTime ：  当前秒杀的time
	 * @return 如果影响行数等于>1，表示更新的记录行数,等于0说明失败
	 */
	int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

	/**
	 * 根据id查询秒杀对象
	 * 
	 * @param seckillId
	 * @return
	 */
	Seckill queryById(long seckillId);

	/**
	 * 根据偏移量查询秒杀商品列表
	 * 
	 * @param offset
	 * @param limit
	 * @return
	 */
	//				         利用‘@Param’告诉sql中的   叫"offset"的这个形参(占位符)的实参为offset(传入的实际值)，叫"limit"的这个形参(占位符)"limit"的实参为limit(传入的实际值) 
	List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);
	//							           形参			   实参				  形参			   实参
	/**
	 * 使用存储过程执行秒杀
	 * 
	 * @param paramMap
	 */
	void killByProcedure(Map<String, Object> paramMap);

}
