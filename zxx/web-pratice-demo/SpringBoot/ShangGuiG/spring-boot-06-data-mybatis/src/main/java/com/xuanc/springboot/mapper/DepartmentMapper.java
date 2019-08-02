package com.xuanc.springboot.mapper;

import com.xuanc.springboot.bean.Department;
import org.apache.ibatis.annotations.*;

/**
 * <code>@Mapper</code> 指定这是一个数据库操作的 mapper
 * @author      xuanc
 * @date        2019/8/1 下午4:45
 * @version     1.0
 */
// @Mapper
public interface DepartmentMapper {

    @Select("select * from department where id=#{id}")
    public Department getDeptById(Integer id);

    @Delete("delete from department where id=#{id} ")
    public int deleteDeptById(Integer id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into department (department_name) values (#{departmentName} );")
    public int insertDept(Department department);

    @Update("update department set department_name=#{departmentName} where id=#{id}")
    public int updateDept(Department department);

}
