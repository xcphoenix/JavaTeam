package com.xuanc.springboot.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.xuanc.springboot.dao.DepartmentDao;
import com.xuanc.springboot.dao.EmployeeDao;
import com.xuanc.springboot.entities.Department;
import com.xuanc.springboot.entities.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * ClassName    spring-boot-04-web-restfulcrud-EmployeeController
 * Description  
 *
 * @author      xuanc
 * @date        2019/7/24 上午11:25
 * @version     1.0
 */
@Controller
public class EmployeeController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EmployeeDao employeeDao;

    @Autowired
    DepartmentDao departmentDao;

    /**
     * 查询所有员工返回列表页面
     */
    @GetMapping("/emps")
    public String list(Model model) {

        Collection<Employee> employees = employeeDao.getAll();

        model.addAttribute("emps", employees);
        return "emp/list";
    }

    @GetMapping("/emp")
    public String toAddPage(Model model) {
        Collection<Department> departments = departmentDao.getDepartments();
        model.addAttribute("depts", departments);
        // 来到添加页面
        return "emp/add";
    }

    @PostMapping("/emp")
    // 自动将请求参数和入参对象一一绑定：要求请求参数的名字和JavaBean入参的对象的属性名
    public String addEmp(Employee employee) {

        logger.info("employee message ==> " + employee);

        employeeDao.save(employee);

        return "redirect:/emps";
    }

    @GetMapping("/emp/{id}")
    public String toEditPage(@PathVariable("id") Integer id,
                             Model model) {
        Employee employee =  employeeDao.get(id);
        model.addAttribute("emp", employee);

        Collection<Department> departments = departmentDao.getDepartments();
        model.addAttribute("depts", departments);

        logger.info("employee ==> " + employee.toString() + ", depts ==> " + departments);

        // 回到修改页面，直接重用 add.html
        return "emp/add";
    }

    @PutMapping("/emp")
    public String updateEmployee(Employee employee) {
        logger.info("修改 -- 员工信息 >> " + employee);
        employeeDao.save(employee);
        return "redirect:/emps";
    }

    @DeleteMapping("/emp/{id}")
    public String deleteEmp(@PathVariable("id") Integer id) {
        logger.info("删除员工 id = " + id);
        employeeDao.delete(id);
        return "redirect:/emps";
    }

}
