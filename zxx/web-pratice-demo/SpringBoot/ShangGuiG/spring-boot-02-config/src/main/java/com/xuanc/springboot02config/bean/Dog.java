package com.xuanc.springboot02config.bean;

import org.springframework.stereotype.Component;

/**
 * ClassName    spring-boot-02-config-Dog
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/19 下午2:57
 * @version     1.0
 */
public class Dog {

    private String name;
    private Integer age;

    @Override
    public String toString() {
        return "Dog{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
