package com.xuanc.springboot.controller;

import com.xuanc.springboot.exception.UserNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName    spring-boot-04-web-restfulcrud-MyExceptionHandler
 * Description
 *
 * @author xuanc
 * @version 1.0
 * @date 2019/7/26 下午9:41
 */
@ControllerAdvice
public class MyExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);

    // 都返回 JSON
    // @ResponseBody
    // @ExceptionHandler(UserNotExistException.class)
    // public Map<String, Object> handleException(Exception e) {
    //     logger.error("发生异常！\n");
    //     e.printStackTrace();
    //
    //     Map<String, Object> map = new HashMap<>();
    //     map.put("errorCode", 400);
    //     map.put("message", e.getMessage());
    //     return map;
    // }


    @ExceptionHandler(UserNotExistException.class)
    public String handleException(Exception e, HttpServletRequest request) {
        logger.error("发生异常！\n");
        e.printStackTrace();
        // 设置自己的错误状态码
        request.setAttribute("javax.servlet.error.status_code", 500);
        Map<String, Object> map = new HashMap<>();
        map.put("aaa", "user not exist");
        map.put("message", "用户出错了");
        request.setAttribute("ext", map);
        return "forward:/error";
    }
}
