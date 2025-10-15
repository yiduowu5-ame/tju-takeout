package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController //标记这个类负责处理http请求，所有返回值会自动转化为json
@RequestMapping("/admin/employee") //定义请求路径前缀，该类的所有映射都会以/admin/employee开头
@Slf4j   //lombok,自动为类生成一个日志
@Api(tags="员工相关接口")
public class EmployeeController {

    @Autowired //自动依赖注入,Spring 会找到一个类型为 EmployeeService 的 Bean，并赋值给这个字段。
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")//接受post /admin/employee/login
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工登出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 员工创建
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("员工创建")
    public Result<EmployeeDTO> create(@RequestBody EmployeeDTO employeeDTO) {
        log.info("员工创建：{}", employeeDTO);
        employeeService.create(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询，参数为：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);//所有分页查询都要封装成一个pageResult对象
        return Result.success(pageResult);
    }

    /**
     * 禁启用员工
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改员工状态")
    public Result<Void> changeStatus(@PathVariable Integer status,Long id) {
        log.info("修改员工状态，当前员工id:{},状态变为:{}", id, status);
        employeeService.changeStatus(status, id);
        return Result.success();
    }

    /**
     * 按id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("按照id查询员工")
    public Result<Employee> QueryById(@PathVariable Long id) {
        log.info("按照id查询员工，当前id为{}",id);
        Employee employee = employeeService.QueryByID(id);
        return  Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result<Void> update(@RequestBody EmployeeDTO employeeDTO){
        Long targetEmployeeId = employeeDTO.getId();
        log.info("员工信息修改，修改员工id为:{}", targetEmployeeId);
        employeeService.updateEmployee(employeeDTO);
        return Result.success();
    }

}
