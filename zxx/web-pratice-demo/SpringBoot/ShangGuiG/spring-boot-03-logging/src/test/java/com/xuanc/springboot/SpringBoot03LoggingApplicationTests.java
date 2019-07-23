package com.xuanc.springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBoot03LoggingApplicationTests {

    // 记录器
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void contextLoads() {
        // 日志的级别，由低到高 -------------------
        // 可以调整输出的日志的级别；日志就只会在这个级别的以后的高级别生效
        // 跟踪轨迹
        logger.trace("这是 trace 日志...");
        // 调试信息
        logger.debug("这是 debug 日志...");
        // SpringBoot 默认只会输出 info 级别：root 级别
        // info
        logger.info("这是 info 日志...");
        // warning
        logger.warn("这是 warning 日志...");
        // error
        logger.error("这是 error 日志...");
    }

}
