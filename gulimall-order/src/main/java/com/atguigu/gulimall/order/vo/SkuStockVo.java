package com.atguigu.gulimall.order.vo;

import lombok.Data;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 20:56
 * @Description:
 **/
@Data
public class SkuStockVo {
    private Long skuId;

    private Boolean hasStock;
}

