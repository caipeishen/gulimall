package com.atguigu.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 18:05
 * @Description: 物流服务远程调用
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

}
