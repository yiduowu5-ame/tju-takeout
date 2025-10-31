package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ReportMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //用于存放从begin到end每天的日期
        List<LocalDate>dateList = new ArrayList<>();

        while(begin.isBefore(end)||begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //存放每天营业额
        List<Double>turnoverList =new ArrayList<>();

        //获取营业额，通过查询每一天的营业额得出
        for(LocalDate date:dateList) {
            //select sum(amount)
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//得到这一天的最早时间，eg:2025-10-30 00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer status = Orders.COMPLETED;

            Double turnover =  orderMapper.countStatistics(beginTime,endTime,status);
            turnover = turnover==null?0.0:turnover;

            turnoverList.add(turnover);

        }


        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList,","));
        turnoverReportVO.setDateList(StringUtils.join(dateList,","));
        return turnoverReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        //用于存放从begin到end每天的日期
        List<LocalDate>dateList = new ArrayList<>();

        while(begin.isBefore(end)||begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //存放每日订单数
        List<Integer>orderCountList =new ArrayList<>();
        //存放每日有效订单数
        List<Integer>validOrderCountList =new ArrayList<>();

        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        for(LocalDate date:dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Integer status = Orders.COMPLETED;
            //查询数据库
            Integer orderCount = orderMapper.orderCountStatistics(beginTime,endTime);
            Integer validOrderCountPerDay = orderMapper.validOrderCountStatistics(beginTime,endTime,status);
            //记录总数
            totalOrderCount +=orderCount;
            validOrderCount +=validOrderCountPerDay;
            //加入至list中
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCountPerDay);
        }


        Double orderCompleteRate =  totalOrderCount == 0?0.0:((double)validOrderCount/totalOrderCount);

        return   OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompleteRate)
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //用于存放从begin到end每天的日期
        List<LocalDate>dateList = new ArrayList<>();

        while(begin.isBefore(end)||begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //用户总量
        List<Integer>userCountList =new ArrayList<>();
        //新增用户
        List<Integer>newUserCountList =new ArrayList<>();

        for(LocalDate date:dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Integer user = userMapper.totalUserStatistics(beginTime,endTime);
            Integer newUser = userMapper.newUserStatistics(beginTime,endTime);

            userCountList.add(user);
            newUserCountList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(userCountList, ","))
                .newUserList(StringUtils.join(newUserCountList,","))
                .build();
    }

    /**
     * 销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Integer status = Orders.COMPLETED;

        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.top10(beginTime, endTime, status);

        //从DTO中取出name和number的list
        List<String> names = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }
}
