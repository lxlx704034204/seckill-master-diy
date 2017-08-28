package org.seckill;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 配置spring和junit整合  
 * jar: spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class) //junit启动时加载springIOC容器
// 告诉junit 本项目的spring配置文件位置和name(以验证mybatis整合spring、db连接池是否正常):
@ContextConfiguration({ "classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml" })
public abstract class BaseTest {
//如果这个BaseTest类 不用abstract来修饰的话，就会报如下错误：
//initializationError(org.seckill.BaseTest) Time elapsed: 0.004 sec <<< ERROR!
//java.lang.Exception: No runnable methods   ！！！
	
}
