package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void create(DishDTO dishDTO) {
        //向菜品表中插入一条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        //获取insert语句生成的主键值,主键返回
        Long id = dish.getId();
        //向口味表插入多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(!dishDTO.getFlavors().isEmpty()){
            flavors.forEach(f->{
                f.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.page(dishPageQueryDTO);

        //构建需要的PageResult
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 按id查询菜品
     *
     * @param id
     * @return
     */
    public DishVO queryById(Long id){
        Dish dish = dishMapper.getById(id);

        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

//    /**
//     * 按分类id查询菜品
//     *
//     * @param id
//     */
//    public void queryByCategory(Long id){
//        dishMapper.queryByCategory(id);
//    }

    /**
     * 菜品的起售和停售
     *
     * @param status
     * @param id
     */
    public void changeStatus(Integer status, Long id){
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
    }

    /**
     * 删除菜品
     * @param ids
     */
    public void delete(List<Long> ids){
        //判断当前菜品能否删除
        //1.是否存在起售中的菜
        for(Long id:ids){
            Dish dish = dishMapper.getById(id);
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.是否关联某套餐

        List<Long> setmealIds = setmealDishMapper.getSetmealDishIdByDishId(ids);
        if( setmealIds!=null&& ! setmealIds.isEmpty()){
            throw  new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品中的菜品数据

        for(Long id:ids){
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);//删除菜品关联的口味数据
        }
        return;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    public void update(DishDTO dishDTO){
        //操作菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //操作菜品关联口味信息
        //更新菜品口味，采用删掉所有的口味，再添加

        Long id = dishDTO.getId();
        dishFlavorMapper.deleteByDishId(id);

        List<DishFlavor>flavors = dishDTO.getFlavors();
        //添加
        if(!dishDTO.getFlavors().isEmpty()){
            flavors.forEach(f->{
                f.setDishId(id);
            });
        dishFlavorMapper.insertBatch(flavors);
        }
    }
}
