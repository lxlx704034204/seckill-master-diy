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

//@Componet(���е������ע��(����Ƚϴ�<ģ��>�����֪�������������þ����ע��(eg:@Service @Dao @Controller)):); @Service; @Dao; @Controller;
@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// slf4j��ʵ�� ��logback.xml ������!!!
	//logback.qos.ch/manual/cofiguration.html

	// ע��Service���� @Autowired @Resource @Inject
	@Autowired
	private SeckillDao seckillDao;

	@Autowired //mybatis��implements����Dao��ӿڣ�Ȼ������spring���г�ʼ�� �� �����á�@Autowired��ע���  ������ע��
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
 
	//����ɱ������ʱ������ ��ɱinterface��ַ���������ϵͳtime����ɱtime
	@Override //�Ƿ�¶ ��ɱ�ӿڵ�ַ(������������ �Ƿ�����ɱ �ķ�����)
	public Exposer exportSeckillUrl(long seckillId) {
		logger.info("-����-0-��");
		
		// �Ż��㣺�����Ż�����ʱ�Ļ�����ά��һ���� 
		Seckill seckill = redisDao.getSeckill(seckillId);// 1.����Dao
		if (seckill == null) {
			logger.info("-����-0.1-��");
			seckill = seckillDao.queryById(seckillId);	 // 2.�������ݿ�
			if (seckill == null) { // seckill == null ������ɱδ����(��exposed=false����¼)
				logger.info("-����-0.2-��");
				return new Exposer(false, seckillId);
			} else {
				logger.info("-����-0.3-��");
				redisDao.putSeckill(seckill);			 // 3.����redis
			}
		}
		logger.info("-����-1-��");
		if (seckill == null) {
			logger.info("-����-1.2-��");
			return new Exposer(false, seckillId);
		}
		
		Date startTime = seckill.getStartTime(); //seckillId��Ӧ����Ʒ�� ��ɱ��ʼtime
		Date endTime = seckill.getEndTime();	 //seckillId��Ӧ����Ʒ�� ��ɱ����time	 
		Date nowTime = new Date();				 //ϵͳ��ǰʱ��
		
		logger.info("-����-2.1-��"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime));
		logger.info("-����-2.2-��"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTime));
		logger.info("-����-2.3-��"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime));
		
		if ( nowTime.getTime() < startTime.getTime() 			//������ɱ��û��ʼ
		  || nowTime.getTime() > endTime.getTime()) {		//������ɱ�Ѿ����� 	
			//  ��    ˵����ɱfalse(��exposed=false����¼,ͬʱ��¼�º���������Ϣ)
			
			logger.info("-����-2.4-��");
			
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		logger.info("-����-3-��");
		
		// ת���ض��ַ����Ĺ��̣�������
		String md5 = getMD5(seckillId);
		logger.info("-����-9-��ӡmd5��"+ md5); //35927ce4449ce0f6600683f1b75e6b47
		
		return new Exposer(true, md5, seckillId);//���������� exposed=false������Ժ���������������exposed����=true��
	}
	private String getMD5(long seckillId) {
		String base = seckillId + "/" + slat; // ��MD5 ���� keyId
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());//DigestUtils: springר��Ӧ��MD5����
		return md5;
	}
	// ���û���������md5��ֵ�ַ��������ڻ���MD5(���ǲ�ϣ���û�ȥ�µ����ǵ� keyId)
	private final String slat = "skdfjksjdf7787%^%^%^FSKJFK*(&&%^%&^8DF8^%^^*7hFJDHFJ";//�����д��Խ����Խ�ã�

	
	
	@Override
	@Transactional //��Springע��ʹ�� ������˳��÷�����ʱ���Զ���������control���߼�
	/**
	 * ʹ��ע��������񷽷����ŵ㣺 1.�����ŶӴ��һ��Լ������ȷ��ע���񷽷��ı�̷��
	 * 2.��֤���񷽷���ִ��ʱ�価���̣ܶ���Ҫ�����������������RPC/HTTP������߰��뵽���񷽷��ⲿ
	 * 3.�������еķ�������Ҫ������ֻ��һ���޸Ĳ�����ֻ����������Ҫ�������
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		if (md5 == null || !md5.equals(getMD5(seckillId))) { 	//if�û������MD5=null ����  (�������� �쳣 || �Ķ�seckillId��)
			throw new SeckillException("seckill data rewrite");	//��dialog ��дmd5
		}
		// ִ����ɱ�߼��� ��¼������Ϊ + �����
		Date now = new Date();
		
//		try {
//			int updateCount = seckillDao.reduceNumber(seckillId, now);// ����棬�ȵ���Ʒ����
//			if (updateCount <= 0) { // û�и��µ���¼ �� ˵����ɱ�ѽ�������ɱδ��ʼ���߿��û���ˣ��� rollback ��ɱ����
//				throw new SeckillCloseException("seckill is closed");
//			} else {
//				int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);// ��¼������Ϊ
//				if (insertCount <= 0) {// �ظ���ɱ(keyId��ͻ��)
//					throw new RepeatKillException("seckill repeated");
//				} else {
//					// ��ɱ�ɹ� commit
//					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
//				  //return new SeckillExecution(seckillId, 1, "��ɱ�ɹ�", successKilled);//�˹��췽�����Ƽ�
//					//				�Ż���		           ����            1, "��ɱ�ɹ�"�����ĳ��� ����Ӧ���� ö�٣����������ֵ䣡����
//					return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);//�Ƽ� �˸����Ĺ��췽��
//				}
//			}
//		} catch (SeckillCloseException e1) {
//			throw e1;
//		} catch (RepeatKillException e2) {
//			throw e2;	
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			// �����б������쳣(���߼�����쳣) ת��Ϊ�������쳣������spring������� rollback��(Ϊ�˼����Ĳ��� ��  ������Ϊ�Ĳ��� ��ͬʱִ�У�)��
//			throw new SeckillException("seckill inner error:" + e.getMessage());//throw ҵ���쳣
//		}
		 
		
		try {
 			int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);// ��¼������Ϊ
 			//								   insert��ʱ����ܻᳬʱ��db�Ͽ� �ȵȣ�so��Ҫ����try...catch...
 			
			// Ψһ��seckillId,userPhone ��������
			if (insertCount <= 0) {// �ظ���ɱ(keyId��ͻ��)
				throw new RepeatKillException("seckill repeated");
			} else {
				int updateCount = seckillDao.reduceNumber(seckillId, now);// ����棬�ȵ���Ʒ����
				if (updateCount <= 0) { // û�и��µ���¼ �� ˵����ɱ�ѽ�������ɱδ��ʼ���߿��û���ˣ��� rollback ��ɱ����
					throw new SeckillCloseException("seckill is closed");
				} else {
					// ��ɱ�ɹ� commit
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
				}
			}
		} catch (SeckillCloseException e1) { //�׳����쳣 ˵�� ��ɱ�ر�
			throw e1;
		} catch (RepeatKillException e2) {	//�׳����쳣 ˵�� �ظ���ɱ
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// �����б������쳣(���߼�����쳣) ת��Ϊ�������쳣������spring������� rollback��(Ϊ�˼����Ĳ��� ��  ������Ϊ�Ĳ��� ��ͬʱִ�У�)��
			throw new SeckillException("seckill inner error:" + e.getMessage());//throw ҵ���쳣
		}
	}

	// �洢���̵���
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
		map.put("result", null);		// ����  OUT r_result INT
		try {
			seckillDao.killByProcedure(map); // ִ�д洢���̣�result����ֵ
			// ��ȡresult
			int result = MapUtils.getInteger(map, "result", -2);
			if (result == 1) { //����ͨ�����ô洢����  ��ɱ�ɹ�
				SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone); //��db���ҵ� ��ɱִ�гɹ����Ǹ� ���� 
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
