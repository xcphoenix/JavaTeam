package com.xuanc.springboot.mapper;

import com.xuanc.springboot.bean.Employee;

/**
 * <code>@Mapper @MapperScan</code>
 * @author xuanc
 * @version 1.0
 * @date 2019/8/2 上午8:08
 */
public interface EmployeeMapper {

    public Employee getEmpById(Integer id);

    public void insertEmp(Employee employee);

}
