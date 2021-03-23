package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 18:05
 * @Description: 物流服务远程调用
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> SkuIds);

}
