package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 用于替换微信支付问题的方法
     * @param orderStatus
     * @param orderPaidStatus
     * @param checkOutTime
     * @param orderNumber
     */
    @Update("update orders set status=#{orderStatus},pay_status = #{orderPaidStatus},checkout_time = #{checkOutTime} where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkOutTime, String orderNumber);

    /**
     * 分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 通过订单ID查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 通过状态获取对应订单数量
     * @param status
     * @return
     */
    @Select("select count(orders.id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 通过订单时间来得到订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByTime(Integer status,LocalDateTime orderTime);

    /**
     * 统计订单销售总数
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    @Select("select sum(orders.amount) from orders where order_time>#{beginTime} and order_time<= #{endTime} and status = #{status}")
    Double countStatistics(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    /**
     * 统计总订单数
     * @param beginTime
     * @param endTime
     * @return
     */
    @Select("select count(*) from orders where order_time>#{beginTime} and order_time <= #{endTime}")
    Integer orderCountStatistics(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 统计有效订单数
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    @Select("select count(*) from orders where order_time>#{beginTime} and order_time <= #{endTime} and status = #{status}")
    Integer validOrderCountStatistics(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    /**
     * 统计指定时间内的top10订单
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO>top10(LocalDateTime beginTime,LocalDateTime endTime,Integer status);
}
