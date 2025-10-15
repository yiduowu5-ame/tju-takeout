package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import org.springframework.web.bind.annotation.PathVariable;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 员工创建
     * @param employeeDTO
     */
    void create(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 修改员工状态
     * @param status
     * @param id
     */
    void changeStatus(Integer status, Long id);

    /**
     * 按id查询员工
     * @param id
     * @return
     */
    Employee QueryByID(Long id);

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    void updateEmployee(EmployeeDTO employeeDTO);

}
