package com.sky.service.impl;

import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    public RedisTemplate<String,Object> redisTemplate;

    /**
     * 设置店铺状态
     * @param status
     */
    public void set(Integer status){
        redisTemplate.opsForValue().set(KEY,status);
    }

    /**
     * 获取店铺状态
     * @return
     */
    public Integer get(){
        return (Integer) redisTemplate.opsForValue().get(KEY);
    }
}
