package com.xuanc.authjwt.service;

import com.xuanc.authjwt.entiy.User;
import com.xuanc.authjwt.mapper.UserMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xuanc
 * @version 1.0
 * @date 2019/8/2 下午4:19
 */
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public User findUserById(Integer userId) {
        return userMapper.getUserById(userId);
    }

    public void register(User user) throws DataAccessException {
        userMapper.addUser(user);
    }

    public User login(User user) {
        return userMapper.loginByName(user);
    }
}
