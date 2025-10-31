package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * *")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);

        List<Orders>timeOutOrders = orderMapper.getByTime(Orders.PENDING_PAYMENT, localDateTime);

        //如果订单不为空
        if(timeOutOrders!=null&& !timeOutOrders.isEmpty()){
            for(Orders orders:timeOutOrders){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveringOrder(){
        log.info("定时处理派送中的订单:{}", LocalDateTime.now());

        List<Orders> orders = orderMapper.getByTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if(orders!=null&& !orders.isEmpty()){
            for(Orders order:orders){
                order.setStatus(Orders.DELIVERY_IN_PROGRESS);
                order.setCancelReason("一直处于派送中的订单,自动完成");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }
}
