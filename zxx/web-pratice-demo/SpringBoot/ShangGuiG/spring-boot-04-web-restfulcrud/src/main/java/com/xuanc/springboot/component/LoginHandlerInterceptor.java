package com.xuanc.springboot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName    spring-boot-04-web-restfulcrud-LoginHandlerInterceptor
 * Description  拦截器－登录检查
 *
 * @author      xuanc
 * @date        2019/7/22 下午5:31
 * @version     1.0
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Object user = request.getSession().getAttribute("loginUser");
        String prefixInfo = ">>> " + request.getRequestURL() + " :: ";

        if (user == null) {
            logger.info(prefixInfo + "被拦截!");
            request.setAttribute("msg", "你还没有登录");
            request.getRequestDispatcher("/login.html").forward(request, response);
            return false;
        } else {
            logger.info(prefixInfo + "放行！");
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
