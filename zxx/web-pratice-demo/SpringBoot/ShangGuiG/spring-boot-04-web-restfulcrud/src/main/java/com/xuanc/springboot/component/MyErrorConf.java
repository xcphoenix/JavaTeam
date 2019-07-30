package com.xuanc.springboot.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Description  
 * <br />
 * @author      xuanc
 * @date        2019/7/30 下午3:08
 * @version     1.0
 */
@Component
@ConfigurationProperties(prefix = "my")
public class MyErrorConf {

    // @Value("${my.include-exception}")
    private boolean includeStacktrace;
    // @Value("${my.include-stacktrace}")
    private boolean includeException;

    boolean isIncludeStacktrace() {
        return includeStacktrace;
    }

    boolean isIncludeException() {
        return includeException;
    }

    @Override
    public String toString() {
        return "MyErrorConf{" +
                "includeException=" + includeException +
                ", includeStackTrace=" + includeStacktrace +
                '}';
    }

    public void setIncludeStacktrace(boolean includeStacktrace) {
        this.includeStacktrace = includeStacktrace;
    }

    public void setIncludeException(boolean includeException) {
        this.includeException = includeException;
    }
}