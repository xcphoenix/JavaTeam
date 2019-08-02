package com.xuanc.authjwt.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.xuanc.authjwt.annotation.PassToken;
import com.xuanc.authjwt.annotation.UserLoginToken;
import com.xuanc.authjwt.entiy.User;
import com.xuanc.authjwt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.ServletSecurityElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 拦截器 - 验证 token
 * @author      xuanc
 * @date        2019/8/2 下午4:26
 * @version     1.0
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 token
        String token = request.getHeader("Authorization");

        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod)handler;
        Method method = handlerMethod.getMethod();

        // 检查是否有 @PassToken 注解
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            return passToken.required();
        }

        // 检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // 执行认证
                if (token == null) {
                    throw new RuntimeException("找不到 token，请重新登录");
                }
                // 获取 token 中的 user id
                String userId;
                Date expireTime;
                try {
                    userId = JWT.decode(token).getAudience().get(0);
                    expireTime = JWT.decode(token).getExpiresAt();
                } catch (JWTDecodeException je) {
                    throw new RuntimeException("401");
                }
                User user = userService.findUserById(Integer.parseInt(userId));
                if (user == null) {
                    throw new RuntimeException("用户不存在，请重新登录");
                } else if (expireTime.getTime() > System.currentTimeMillis()) {
                    throw new RuntimeException("登录已过期，请重新登录");
                }

                // 验证 token
                JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword() +
                        user.getLoginTimestamp() + expireTime.getTime())).build();

                try {
                    jwtVerifier.verify(token);
                } catch (JWTVerificationException jve) {
                    throw new RuntimeException("401");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
