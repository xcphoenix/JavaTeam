package com.xuanc.springboot.controller;

import com.xuanc.springboot.exception.UserNotExistException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ClassName    spring-boot-04-web-restfulcrud-HelloController
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/26 下午4:36
 * @version     1.0
 */
@Controller
public class HelloController {

    @ResponseBody
    @RequestMapping("/hello")
    public String hello(@RequestParam("user") String user) {
        if ("aaa".equals(user)) {
            throw new UserNotExistException();
        }
        return "Hello World";
    }

}
