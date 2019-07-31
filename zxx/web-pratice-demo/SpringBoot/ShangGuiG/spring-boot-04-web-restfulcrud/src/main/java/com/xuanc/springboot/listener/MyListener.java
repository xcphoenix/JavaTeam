package com.xuanc.springboot.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Description  
 * <br />
 * @author      xuanc
 * @date        2019/7/31 上午9:54
 * @version     1.0
 */ 
public class MyListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Web 应用启动...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("当前 Web 项目销毁...");
    }
}
