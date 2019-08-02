package com.xuanc.springboot.controller;

import com.xuanc.springboot.bean.Department;
import com.xuanc.springboot.bean.Employee;
import com.xuanc.springboot.mapper.DepartmentMapper;
import com.xuanc.springboot.mapper.EmployeeMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *   
 * @author      xuanc
 * @date        2019/8/1 下午4:53
 * @version     1.0
 */
@RestController
public class DeptController {

    @Resource
    DepartmentMapper departmentMapper;

    @Resource
    EmployeeMapper employeeMapper;

    @GetMapping("/dept/{id}")
    public Department getDepartment(@PathVariable("id") Integer id) {
        return departmentMapper.getDeptById(id);
    }

    @GetMapping("/dept")
    public Department insertDept(Department department) {
        departmentMapper.insertDept(department);
        return department;
    }

    @GetMapping("/emp/{id}")
    public Employee getEmp(@PathVariable("id") Integer id) {
        return employeeMapper.getEmpById(id);
    }

}
