package com.xuanc.springboot02config02.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName    spring-boot-02-config-02-HelloController
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/20 上午8:47
 * @version     1.0
 */
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "Hello";
    }

}
