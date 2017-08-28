package org.seckill.exception;

/**
 * 秒杀相关业务异常： 把所有编译期异常(或者检查型异常) 转换为运行期异常，这样spring会帮我们 rollback！(为了减库存的操作 与  购买行为的操作 不同时执行！)
 * 
 * @author 李奕锋
 */
public class SeckillException extends RuntimeException {

	public SeckillException(String message) {
		super(message);
	}

	public SeckillException(String message, Throwable cause) {
		super(message, cause);
	}

}
