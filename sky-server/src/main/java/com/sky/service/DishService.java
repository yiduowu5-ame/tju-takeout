package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void create(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 按id查询菜品
     *
     * @param id
     * @return
     */
    DishVO queryById(Long id);

//    /**
//     * 按分类id查询菜品
//     * @param id
//     */
//    void queryByCategory(Long id);

    /**
     * 菜品的起售停售
     * @param status
     * @param id
     */
    void changeStatus(Integer status, Long id);

    /**
     * 删除菜品
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void update(DishDTO dishDTO);
}
