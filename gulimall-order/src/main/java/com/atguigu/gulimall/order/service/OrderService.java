package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author Cai Peishen
 * @email peishen.cai@foxmail.com
 * @date 2021-02-03 10:59:19
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
    
    /**
     * 给订单确认页返回需要的数据
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单操作
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);
    
    /**
     * 根据订单编号获取订单
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 关闭过期的的订单
     */
    void closeOrder(OrderEntity orderEntity);
    
    /**
     * 获取下单信息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);
    
    /**
     * 分页查询会员订单
     * @param params
     * @return
     */
    PageUtils getMemberOrderPage(Map<String, Object> params);
    
    /**
     * 处理支付结果
     * @param payAsyncVo
     */
    void handlerPayResult(PayAsyncVo payAsyncVo);

    /**
     * 秒杀订单
     * @param orderTo
     */
    void createSeckillOrder(SeckillOrderTo orderTo);
}

