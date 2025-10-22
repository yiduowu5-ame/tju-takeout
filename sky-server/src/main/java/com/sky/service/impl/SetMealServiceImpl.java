package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    public void create(SetmealDTO  setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);

        //主键返回，得到套餐的id
        Long id = setmeal.getId();
        //插入套餐中的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(!setmealDishes.isEmpty()){
            //对于套餐中的菜品，给其设置当前的套餐id
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }
        }
        //批量插入
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 按id查询套餐
     * @param id
     * @return
     */
    public SetmealVO queryById(Long id){
        Setmeal setmeal = setmealMapper.getById(id);//通过getById方法得到setmeal中的参数

        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealDishId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    public void delete(List<Long> ids){
        //判断是否可以删除
        //当套餐起售时，不可以删除
        for(Long id:ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if(Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //删除套餐
        for(Long id:ids){
            //删除套餐
            setmealMapper.delete(id);
            //删除套餐关联的菜品
            setmealDishMapper.delete(id);
        }
    }

    /**
     * 套餐的起售停售
     * @param status
     * @param id
     */
    public void changeStatus(Integer status, Long id){
        //起售套餐时，需要判断套餐内是否有停售的菜品，否则无法起售
        if(Objects.equals(status, StatusConstant.ENABLE)){
            List<Dish>dishes = dishMapper.getBySetmealId(id);
            if(dishes!=null&& !dishes.isEmpty()){
                dishes.forEach(dish->{
                    if(StatusConstant.DISABLE.equals(dish.getStatus())){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);

        setmealMapper.update(setmeal);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO){
        Setmeal  setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //1.修改套餐表，执行update
        setmealMapper.update(setmeal);

        //得到套餐id
        Long id = setmealDTO.getId();

        //2.删除套餐和菜品的关联关系，操作setmealDish表
        setmealDishMapper.deleteBySetmealId(id);

        //3.添加套餐关联菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(!setmealDishes.isEmpty()){
            setmealDishes.forEach(dish->{
                dish.setSetmealId(id);
            });
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }
}
