package com.xuanc.j2eechapter17;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
// import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class J2eeChapter17ApplicationTests {

    @Test
    public void contextLoads() {
        // int i = 0;
        // // 连接 redis
        // try (Jedis jedis = new Jedis("localhost", 6379)) {
        //     long start = System.currentTimeMillis();
        //     while (true) {
        //         long end = System.currentTimeMillis();
        //         if (end - start >= 1000) {
        //             break;
        //         }
        //         i++;
        //         jedis.set("test" + i, i + "");
        //     }
        // }
        //
        // System.out.println("redis 每秒操作：" + i + "次");
    }

}
