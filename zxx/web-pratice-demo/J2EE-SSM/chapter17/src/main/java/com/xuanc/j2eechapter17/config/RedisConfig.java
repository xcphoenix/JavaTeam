package com.xuanc.j2eechapter17.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author      xuanc
 * @date        2019/8/5 下午3:18
 * @version     1.0
 */
@Configuration
public class RedisConfig {

    @Bean
    public JedisPoolConfig poolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(50);
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxWaitMillis(20000);
        return jedisPoolConfig;
    }


}
