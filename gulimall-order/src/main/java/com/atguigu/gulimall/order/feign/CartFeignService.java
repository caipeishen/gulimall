package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 18:03
 * @Description: 购物车服务远程调用
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
    
}
