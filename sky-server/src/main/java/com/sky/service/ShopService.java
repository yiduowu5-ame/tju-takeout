package com.sky.service;

public interface ShopService {

    /**
     * 设置店铺状态
     * @param status
     */
    void set(Integer status);

    /**
     * 获取店铺状态
     * @return
     */
    Integer get();
}
