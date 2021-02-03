package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Cai Peishen
 * @Date: 2021/2/3 15:35
 * @Description: 远程调用优惠券服务
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    
    @GetMapping("/coupon/coupon/memberCoupon")
    public R memberCoupon();
    
}
