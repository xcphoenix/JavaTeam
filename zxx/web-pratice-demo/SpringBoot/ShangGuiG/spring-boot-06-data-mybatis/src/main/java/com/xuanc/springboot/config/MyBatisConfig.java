package com.xuanc.springboot.config;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 *   
 * @author      xuanc
 * @date        2019/8/1 下午5:08
 * @version     1.0
 */
@org.springframework.context.annotation.Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(Configuration configuration) {
                // 开启驼峰命名法
                configuration.setMapUnderscoreToCamelCase(true);
            }
        };
    }

}
