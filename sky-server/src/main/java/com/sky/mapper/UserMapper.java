package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

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

    /**
     * 按时间统计用户总数
     * @param beginTime
     * @param endTime
     * @return
     */
    @Select("select COUNT(*) from user where create_time <= #{endTime}")
    Integer totalUserStatistics(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 统计新增用户数
     * @param beginTime
     * @param endTime
     * @return
     */
    @Select("select COUNT(*) from user where create_time>#{beginTime} and create_time <= #{endTime}")
    Integer newUserStatistics(LocalDateTime beginTime, LocalDateTime endTime);
}
