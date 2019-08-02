package com.xuanc.authjwt.mapper;

import com.xuanc.authjwt.entiy.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;

/**
 * @author xuanc
 * @version 1.0
 * @date 2019/8/2 下午4:52
 */
@Mapper
public interface UserMapper {

    int addUser(@Param("user") User user) throws DataAccessException;

    User getUserById(@Param("id") Integer id);

    User loginByName(@Param("user") User user);

}
