package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 18:05
 * @Description: 物流服务远程调用
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    // 获取商品sku是否有库存
    @PostMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> SkuIds);

    // 获取收货地址信息
    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    // 锁定库存
    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(WareSkuLockVo lockVo);
}
