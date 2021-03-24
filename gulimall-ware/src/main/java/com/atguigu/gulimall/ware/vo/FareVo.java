package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/24 9:37
 * @Description:
 **/
@Data
public class FareVo {

    private MemberAddressVo memberAddressVo;

    private BigDecimal fare;
}
