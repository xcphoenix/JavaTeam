package com.xuanc.authjwt.entiy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 使用 lombok 简化实体类的编写
 * @author      xuanc
 * @date        2019/8/2 下午4:05
 * @version     1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;
    private Timestamp loginTimestamp;
}
