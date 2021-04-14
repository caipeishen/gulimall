package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/13 10:40
 * @Description:
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

	@RequestMapping("/product/skuinfo/info/{skuId}")
	R skuInfo(@PathVariable("skuId") Long skuId);
}
