package com.xuanc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ClassName    springboot-01-helloworld-HelloController
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/19 上午9:10
 * @version     1.0
 */
@Controller
public class HelloController {

    @ResponseBody
    @RequestMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

}
