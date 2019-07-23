package com.xuanc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClassName    springboot-01-helloworld-HelloWorldMainApplication
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/19 上午9:05
 * @version     1.0
 */

/**
 * SpringBootApplication 用来标注一个主程序类，说明是一个 Spring Boot 应用
 */
@SpringBootApplication
public class HelloWorldMainApplication {

    public static void main(String[] args) {

        // Spring 应用启动起来
        SpringApplication.run(HelloWorldMainApplication.class, args);

    }

}
