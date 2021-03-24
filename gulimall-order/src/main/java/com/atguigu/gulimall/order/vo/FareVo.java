package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/24 14:11
 * @Description:
 **/
@Data
public class FareVo {
    private MemberAddressVo memberAddressVo;

    private BigDecimal fare;
}
