package com.xuanc.springboot.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Description  
 * <br />
 * @author      xuanc
 * @date        2019/7/31 上午9:49
 * @version     1.0
 */ 
public class MyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("My Filter Process");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
