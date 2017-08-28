package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;

public interface SuccessKilledDao {

	/**
	 * ��¼������Ϊ�����빺����ϸ���ɹ����ظ�
	 * 
	 * @param seckillId
	 * @param userPhone
	 * @return ���������
	 */
	int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

	/**
	 * ����id��ѯSuccessKilled��Я�� Seckill(��ɱ��Ʒ����) ʵ��     	 ��ɱִ�д��Σ�seckillId�� userPhone
	 * 
	 * @param seckillId
	 * @param userPhone
	 * @return
	 */
	SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

}
