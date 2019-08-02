package com.xuanc.springboot.entity;

import javax.persistence.*;

/**
 * 使用 JPA 注解 <code>@Entity</code> 告诉这是一个实体类（和数据库表映射的实体类）
 * <code>@Table(name = "tbl_user")</code> 指定与哪个表对应，省略则默认类名小写
 * @author      xuanc
 * @date        2019/8/2 上午10:09
 * @version     1.0
 */
@Entity
@Table(name = "tbl_user")
public class User {

    /**
     * 主键
     * 自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "last_name", length = 50)
    private String lastName;
    private String email;

    /**
     * 省略 @Column 默认列名就是属性名
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
