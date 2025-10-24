package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

public interface UserService {

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    public User WechatLogin(UserLoginDTO userLoginDTO);
}
