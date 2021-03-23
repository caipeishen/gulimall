package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.vo.OrderConfirmVo;
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
}

