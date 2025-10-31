package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO){
        //处理各种业务异常（地址簿为空、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //地址簿为空，无法正常下单，抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //查询购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        //得到当前userid的购物车数据
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if(list.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders order = Orders.builder()
                .orderTime(LocalDateTime.now())
                .payStatus(Orders.UN_PAID)
                .status(Orders.PENDING_PAYMENT)
                .number(String.valueOf(System.currentTimeMillis()))
                .phone(addressBook.getPhone())
                .address(addressBook.getDetail())
                .consignee(addressBook.getConsignee())
                .userId(userId)
                .build();
        BeanUtils.copyProperties(ordersSubmitDTO, order);

        orderMapper.insert(order);

        //向订单明细表插入n条数据
        //通过购物车数据插入
        Long orderId = order.getId();//主键返回

        List<OrderDetail>details = new ArrayList<>();

        for(ShoppingCart cart :list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);

            orderDetail.setOrderId(orderId);

            //加入进明细数组中
            details.add(orderDetail);
        }
        //批量添加订单明细表
        orderDetailMapper.BatchInsert(details);

        //清空当前用户购物车数据
        shoppingCartMapper.clean(userId);

        //封装VO，返回结果

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderTime(order.getOrderTime())
                .orderAmount(order.getAmount())
                .orderNumber(order.getNumber())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新,多定义一个方法进行修改
        Integer OrderPaidStatus= Orders.PAID;//支付状态,已支付
        Integer OrderStatus =Orders.TO_BE_CONFIRMED; //订单状态,待接单

        //发现没有将支付时间 check_out属性赋值,所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        //获取订单号码
        String orderNumber = ordersPaymentDTO.getOrderNumber();


        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderNumber);

        Orders ordersDB = orderMapper.getByNumber(orderNumber);
        //通过websocket向客户端浏览器发推送消息 type orderId content
        Map map = new HashMap();
        map.put("type",1);//提示类型： 1 来单提醒 2客户催单
        map.put("orderId",ordersDB.getId());//订单号
        map.put("content","订单号:"+orderNumber);

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

    }

    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult page(int page, int pageSize, Integer status){
        PageHelper.startPage(page, pageSize);

        //通过一个DTO来进行封装，用于分页查询
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //分页查询
        Page<Orders>pages = orderMapper.pageQuery(ordersPageQueryDTO);

        //分页查询结果使用一个OrderVO的list进行封装
        List<OrderVO>list = new ArrayList<>();

        if(pages != null&&pages.getTotal()>0){
            for(Orders order:pages){
                //取出每一个订单
                Long orderId = order.getId();

                //查询订单详细
                List<OrderDetail>details = orderDetailMapper.getByOrderId(orderId);

                //用OrderVO封装
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(details);

                list.add(orderVO);
            }
        }
        return new PageResult(pages.getTotal(),list);
    }

    /**
     * 查询订单的详细信息
     * @param id
     * @return
     */
    @Override
    public OrderVO detail(Long id) {
        Orders order = orderMapper.getById(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);

        //通过订单id查出订单详细信息
        List<OrderDetail>details = orderDetailMapper.getByOrderId(order.getId());

        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    /**
     * 催单
     * @param id
     */
    public void reminder(Long id){
        Orders order = orderMapper.getById(id);

        Map map = new HashMap();
        map.put("type",2);//提示类型： 1 来单提醒 2客户催单
        map.put("orderId",order.getId());//订单号
        map.put("content","订单号:"+order.getNumber());

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 取消订单
     * @param id
     */
    public void cancel(Long id) throws Exception{
        //查询订单id
        Orders order = orderMapper.getById(id);

        //检验订单是否存在
        if(order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断是否在在待接单状态及以前取消
        if(order.getStatus() > Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //订单在待接单状态下取消，需要退款
        if(order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
//            weChatPayUtil.refund(
//                    order.getNumber(),//订单号
//                    order.getNumber(),//退款单号
//                    new BigDecimal(0.01),//退款金额
//                    new BigDecimal(0.01)//原订单金额
//            );
            //设置支付状态
            order.setPayStatus(Orders.REFUND);
        }

        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());

        orderMapper.update(order);
    }

    /**
     * 再来一单
     * @param id
     */
    public void repetition(Long id){
        //相当于将该id的商品重新放入购物车中
        //获取订单详细
        List<OrderDetail>details = orderDetailMapper.getByOrderId(id);

        //将订单详细内容转化为购物车内容
        List<ShoppingCart>shoppingCartList = details.stream().map(x->{
            ShoppingCart shoppingCart = new ShoppingCart();

            BeanUtils.copyProperties(x,shoppingCart,"id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.BatchInsert(shoppingCartList);
    }

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders>pages = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO>list = getOrderVOList(pages);

        return new PageResult(pages.getTotal(),list);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> pages) {
        List<OrderVO> list = new ArrayList<>();

        List<Orders>orders = pages.getResult();
        if(!CollectionUtils.isEmpty(orders)){
            for(Orders order:orders){
                //将相同字段复制到dishVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);

                String OrderDishes = getOrderDishes(order);

                orderVO.setOrderDishes(OrderDishes);
                list.add(orderVO);
            }
        }

        return list;
    }

    /**
     * 根据订单获得菜品信息字符串
     * @param orders
     * @return
     */
    private String getOrderDishes(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            return x.getName() + "*" + x.getNumber() + ";";
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    public OrderStatisticsVO statistics(){
        Integer toBeConfirmed =orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) throws Exception{
        Long orderId = ordersCancelDTO.getId();
        Orders order = orderMapper.getById(orderId);

        //获取支付状态
        Integer status = order.getStatus();
        //如果已经支付，需要退款(忽略微信支付部分)
        if(Objects.equals(status, Orders.PAID)){
            log.info("退款");
        }

        Orders orders = new Orders();
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setId(orderId);

        orderMapper.update(orders);
    }

    /**
     * 管理端拒单
     * @param ordersRejectionDTO
     * @throws Exception
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Long orderId = ordersRejectionDTO.getId();
        //获取对应订单
        Orders order = orderMapper.getById(orderId);
        //订单只有存在且状态为待接单时才可以拒单
        if(order==null|| !Objects.equals(order.getStatus(), Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //退款
        log.info("退款");

        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());

        orderMapper.update(orders);
    }

    /**
     * 管理端接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Long  orderId = ordersConfirmDTO.getId();

        Orders order = orderMapper.getById(orderId);
        //只有在状态为待支付时才可以接单
        if(order==null|| !Objects.equals(order.getStatus(), Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //获取订单
        Orders order = orderMapper.getById(id);

        //只有订单为确认状态才可以派送
        if(order==null|| !Objects.equals(order.getStatus(), Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        //获取订单
        Orders order = orderMapper.getById(id);

        //只有订单为送达中状态才可以完成
        if(order==null|| !Objects.equals(order.getStatus(), Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }
}
