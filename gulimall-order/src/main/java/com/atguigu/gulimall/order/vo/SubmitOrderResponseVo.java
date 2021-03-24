package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/24 10:53
 * @Description:
 **/
@Data
public class SubmitOrderResponseVo {

    private OrderEntity orderEntity;

    /**
     * 错误状态码： 0----成功
     */
    private Integer code;
}

