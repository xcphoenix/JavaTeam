package com.xuanc.springboot.exception;

/**
 * ClassName    spring-boot-04-web-restfulcrud-UserNotExist
 * Description  自定义异常－用户不存在
 *
 * @author      xuanc
 * @date        2019/7/26 下午4:34
 * @version     1.0
 */ 
public class UserNotExistException extends RuntimeException {
    public UserNotExistException() {
        super("用户不存在");
    }
}
