package com.xuanc.springboot.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置 Druid 数据源
 * @author      xuanc
 * @date        2019/8/1 上午11:29
 * @version     1.0
 */
@Configuration
public class DruidConfig {

    /**
     * 创建数据源，加载到容器中，并将配置的属性绑定到数据源中
     * @return DruidDataSource
     */
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource druid() {
        return new DruidDataSource();
    }

    /**
     * 配置 Druid 的监控
     * 1. 配置一个管理后台的 Servlet
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        // /druid/* not /druid/
        Map<String, String> initParams = new HashMap<>(5);

        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "123456");

        bean.setInitParameters(initParams);
        return bean;
    }

    /**
     * 2. 配置一个 web 监控的 Filter
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebStatFilter());

        Map<String, String> initParams = new HashMap<>(5);
        initParams.put("exclusions", "*.js, *.css, /druid/*");
        bean.setInitParameters(initParams);
        bean.setUrlPatterns(Collections.singletonList("/*"));

        return bean;
    }

}
