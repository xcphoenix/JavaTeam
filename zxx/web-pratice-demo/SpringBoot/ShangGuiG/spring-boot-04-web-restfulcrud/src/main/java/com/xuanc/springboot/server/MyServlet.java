package com.xuanc.springboot.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Description  
 * <br />
 * @author      xuanc
 * @date        2019/7/30 下午9:40
 * @version     1.0
 */ 
public class MyServlet extends HttpServlet {

    private void service(HttpServletResponse resp) throws IOException {
        resp.getWriter().write("Hello Servlet In SpringBoot");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(resp);
    }
}
