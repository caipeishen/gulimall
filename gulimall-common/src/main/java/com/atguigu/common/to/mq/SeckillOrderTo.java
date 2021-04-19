package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/19 11:02
 * @Description: 秒杀订单TO
 */
@Data
public class SeckillOrderTo {

	/**
	 * 秒杀订单id
	 */
	private String orderSn;

	/**
	 * 活动场次id
	 */
	private Long promotionSessionId;
	/**
	 * 商品id
	 */
	private Long skuId;
	/**
	 * 秒杀价格
	 */
	private BigDecimal seckillPrice;
	/**
	 * 秒杀总量
	 */
	private Integer num;

	/**
	 * 会员id
	 */
	private Long memberId;
}
