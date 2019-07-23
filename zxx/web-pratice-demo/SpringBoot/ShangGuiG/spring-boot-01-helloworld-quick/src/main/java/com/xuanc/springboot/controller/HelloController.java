package com.xuanc.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName    spring-boot-01-helloWorld-quick-HelloController
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/19 下午2:06
 * @version     1.0
 */
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() {
        return "hello world quick";
    }

}
