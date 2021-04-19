package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 18:03
 * @Description: 购物车服务远程调用
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

	@GetMapping("/product/spuinfo/skuId/{id}")
	R getSkuInfoBySkuId(@PathVariable("id") Long skuId);

	@RequestMapping("product/skuinfo/info/{skuId}")
	R info(@PathVariable("skuId") Long skuId);
}
