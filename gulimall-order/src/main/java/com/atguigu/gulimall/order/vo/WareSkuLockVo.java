package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/24 10:59
 * @Description:
 **/
@Data
public class WareSkuLockVo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;

}
