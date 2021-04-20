package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 18:03
 * @Description: 秒杀远程服务熔断
 */
@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {

	@Override
	public R getSkuSeckillInfo(Long skuId) {
		log.info("服务熔断...getSkuSeckillInfo");
		return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
	}
	
}
