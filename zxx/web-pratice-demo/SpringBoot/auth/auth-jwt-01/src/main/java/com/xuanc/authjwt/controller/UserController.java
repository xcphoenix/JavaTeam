package com.xuanc.authjwt.controller;

import com.xuanc.authjwt.annotation.PassToken;
import com.xuanc.authjwt.annotation.UserLoginToken;
import com.xuanc.authjwt.entiy.User;
import com.xuanc.authjwt.service.TokenService;
import com.xuanc.authjwt.service.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author      xuanc
 * @date        2019/8/2 下午4:52
 * @version     1.0
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private TokenService tokenService;

    @PassToken
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        user.setLoginTimestamp(new Timestamp(System.currentTimeMillis()));
        Map<String, String> resultMap = new HashMap<>(5);
        try {
            userService.register(user);
        } catch (DataAccessException dae) {
            resultMap.put("error", "用户已被注册！");
            return resultMap;
        }
        String token = tokenService.getToken(user);
        resultMap.put("msg", "注册成功");
        resultMap.put("token", token);
        return resultMap;
    }

    @PassToken
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        Map<String, String> resultMap = new HashMap<>(5);
        User userLogin = userService.login(user);
        if (userLogin == null) {
            resultMap.put("error", "用户名或密码错误！");
            return resultMap;
        }
        resultMap.put("msg", "登录成功!");
        String token = tokenService.getToken(userLogin);
        resultMap.put("token", token);
        return resultMap;
    }

    @GetMapping("/message")
    @UserLoginToken
    public Map<String, String> getMessage() {
        Map<String, String> resultMap =  new HashMap<String, String>(5);
        resultMap.put("msg", "测试成功!");
        return resultMap;
    }

}
