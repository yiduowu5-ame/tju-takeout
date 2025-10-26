package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "套餐相关接口")
@RequestMapping ("/admin/setmeal")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result<Void>create(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐，参数为：{}",setmealDTO);
        setMealService.create(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult>page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询，参数为：{}",setmealPageQueryDTO);
        PageResult pageResult = setMealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 按id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("按id查询套餐")
    public Result<SetmealVO>queryById(@PathVariable Long id){
        log.info("按id查询套餐，当前id为:{}",id);
        SetmealVO setmealVO = setMealService.queryById(id);
        return Result.success(setmealVO);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result<Void>delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐");
        setMealService.delete(ids);
        return Result.success();
    }

    /**
     * 套餐的起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐的起售停售")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result<Void>changeStatus(@PathVariable Integer status,Long id){
        log.info("套餐的起售停售操作，目标id:{},修改前状态为:{}",id,status);
        setMealService.changeStatus(status,id);
        return Result.success();
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result<Void>update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐，参数为：{}",setmealDTO);
        setMealService.update(setmealDTO);
        return  Result.success();
    }
}
