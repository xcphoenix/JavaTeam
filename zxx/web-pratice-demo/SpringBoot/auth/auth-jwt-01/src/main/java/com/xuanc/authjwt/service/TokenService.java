package com.xuanc.authjwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.xuanc.authjwt.entiy.User;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author      xuanc
 * @date        2019/8/2 下午9:22
 * @version     1.0
 */ 
@Service
public class TokenService {
    
    private static final long MINUTE_TIME = 60 * 1000;
    private static final long EXPIRE_TIME = 10 * MINUTE_TIME;

    public String getToken(User user) {
        String token = "";
        Date expireTime = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        token = JWT.create().withAudience(user.getId().toString())
                // 设置过期时间为 10 分钟
                .withExpiresAt(expireTime)
                // 使用用户密码、登录时间和过期时间作为秘钥
                .sign(Algorithm.HMAC256(user.getPassword() +
                        user.getLoginTimestamp() + expireTime.getTime()));
        return token;
    }
    
}
