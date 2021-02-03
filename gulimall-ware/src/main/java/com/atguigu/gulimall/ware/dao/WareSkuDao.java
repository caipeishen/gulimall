package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author Cai Peishen
 * @email peishen.cai@foxmail.com
 * @date 2021-02-03 11:07:13
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
