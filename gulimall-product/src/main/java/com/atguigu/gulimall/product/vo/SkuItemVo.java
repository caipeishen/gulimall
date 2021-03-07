package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/7 15:55
 * @Description: 商品详情VO
 **/
@Data
public class SkuItemVo {

    /**
     * 基本信息
     */
    private SkuInfoEntity info;

    private boolean hasStock = true;

    /**
     * 图片信息
     */
    private List<SkuImagesEntity> images;

    /**
     * 销售属性组合
     */
    private List<ItemSaleAttrVo> saleAttr;

    /**
     * 介绍
     */
    private SpuInfoDescEntity desc;

    /**
     * 参数规格信息
     */
    private List<SpuItemAttrGroup> groupAttrs;

    /**
     * 秒杀信息
     */
    private SeckillInfoVo seckillInfoVo;
}

