package com.xuanc.springboot.config;

import com.xuanc.springboot.component.LoginHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * ClassName    spring-boot-04-web-restfulcrud-MyMvcConfig
 * Description  {@code WebMvcConfigurerAdapter} 该类以废弃，应该直接实现
 * {@link WebMvcConfigurer} 接口或继承 {@link WebMvcConfigurationSupport}
 *
 * @author xuanc
 * @version 1.0
 * @date 2019/7/22 下午4:06
 */
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("login");
                registry.addViewController("/login.html").setViewName("login");
                registry.addViewController("/main.html").setViewName("dashboard");
            }

            // 添加拦截器
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new LoginHandlerInterceptor())
                        .excludePathPatterns("/", "/login.html", "/user/login", "/asserts/**", "/favicon.ico")
                        .addPathPatterns("/**");
            }

        };
    }

}
