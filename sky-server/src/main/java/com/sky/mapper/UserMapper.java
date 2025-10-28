package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 通过openid查询用户
     *
     * @param openid
     * @return
     */
    @Select("select * from user where id = #{openid}")
    User getByUserID(String openid);

    /**
     * 新建用户
     * @param user
     */
    void insert(User user);

    /**
     * 通过用户id得到用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);
}
