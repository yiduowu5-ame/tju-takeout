package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新建菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新建菜品")
    public Result<Void> create(@RequestBody DishDTO dishDTO){
        log.info("新建菜品，名字为：{}", dishDTO.getName());
        dishService.create(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询，参数为：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 按id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("按id查询菜品")
    public Result<DishVO>queryById(@PathVariable Long id){
        log.info("按id查询菜品：{}",id);
        DishVO dishVO = dishService.queryById(id);
        return Result.success(dishVO);
    }

    /**
     * 按分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("按分类id查询菜品")
    public Result<List<Dish>>queryByCategory(Long categoryId){
       log.info("按分类id查询菜品：{}",categoryId);
       List<Dish>list = dishService.queryByCategory(categoryId);
       return Result.success(list);
    }

    /**
     * 菜品的起售和停售
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售和停售")
    public Result<Void>changeStatus(@PathVariable Integer status,Long id){
        log.info("菜品的起售和停售，修改前状态为：{}",status);
        dishService.changeStatus(status,id);
        return Result.success();
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result<Void>delete(@RequestParam List<Long> ids){
        log.info("删除菜品，目标为：{}",ids);
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result<Void>update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品，参数为:{}",dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }
}
