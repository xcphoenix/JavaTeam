package com.xuanc.springboot.component;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Description
 *
 * @author      xuanc
 * @date        2019/7/30 上午11:19
 * @version     1.0
 */
@Component
public class MyErrorAttributes extends DefaultErrorAttributes {

    private final MyErrorConf myErrorConf;

    public MyErrorAttributes(MyErrorConf myErrorConf) {
        super(myErrorConf.isIncludeException());
        this.myErrorConf = myErrorConf;
        LoggerFactory.getLogger(this.getClass()).info(this.myErrorConf.toString());
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        Map<String, Object> map = super.getErrorAttributes(webRequest, myErrorConf.isIncludeStacktrace());
        map.put("testMsg", "hhhh");
        Map<String, Object> ext = (Map<String, Object>) webRequest.getAttribute("ext", 0);
        map.put("ext", ext);
        return map;
    }
}