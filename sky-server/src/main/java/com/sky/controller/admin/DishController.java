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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

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

        //清理缓存
        cacheClean(dishDTO.getCategoryId());
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

        //清理缓存
        //得到菜品对应分类
        DishVO dishVO = dishService.queryById(id);
        cacheClean(dishVO.getCategoryId());
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
        //清理缓存
        //找出删除菜品id对应的分类id
        List<Dish>list = dishService.batchQueryById(ids);
        Set<Long> categoryIds = list.stream().map(Dish::getCategoryId).collect(Collectors.toSet());

        dishService.delete(ids);

        categoryIds.forEach(this::cacheClean);
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

        DishVO oldDish =  dishService.queryById(dishDTO.getId());
        Long oldCategoryId = oldDish.getCategoryId();
        Long newCategoryId = dishDTO.getCategoryId();

        //清理旧缓存
        cacheClean(oldCategoryId);

        //修改菜品
        dishService.update(dishDTO);

        //删除修改后的缓存
        cacheClean(newCategoryId);

        return Result.success();
    }

    private void cacheClean(Long categoryId){
        String key = "dish_"+categoryId;
        redisTemplate.delete(key);
    }
}
