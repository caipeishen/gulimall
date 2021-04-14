package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/13 10:40
 * @Description:
 */
@Data
public class SeckillSessionsWithSkus {

	private Long id;
	/**
	 * 场次名称
	 */
	private String name;
	/**
	 * 每日开始时间
	 */
	private Date startTime;
	/**
	 * 每日结束时间
	 */
	private Date endTime;
	/**
	 * 启用状态
	 */
	private Integer status;
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 关联商品
	 */
	private List<SeckillSkuRelationEntity> relationSkus;
}
