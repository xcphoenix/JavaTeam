package com.xuanc.springboot02config.config;

import com.xuanc.springboot02config.service.HelloService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName    spring-boot-02-config-MyAppConfig
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/19 下午7:38
 * @version     1.0
 */
@Configuration
public class MyAppConfig {

    @Bean
    public HelloService helloService() {
        return new HelloService();
    }

}
