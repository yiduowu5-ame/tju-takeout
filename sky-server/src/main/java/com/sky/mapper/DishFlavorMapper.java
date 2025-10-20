package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 按菜品id删除口味表中数据
     * @param dishId
     */
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品id查口味表中数据
     * @param dishId
     * @return
     */
    List<DishFlavor> getByDishId(Long dishId);
}
