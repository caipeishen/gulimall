package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.vo.MemberRsepVo;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;
    
    @Autowired
    private CartFeignService cartFeignService;
    
    @Autowired
    private WmsFeignService wmsFeignService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }
    
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        
        MemberRsepVo memberRsepVo = LoginUserInterceptor.threadLocal.get();
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // 1.远程查询所有的收获地址列表
        List<MemberAddressVo> address;
        try {
            address = this.memberFeignService.getAddress(memberRsepVo.getId());
            confirmVo.setAddress(address);
        } catch (Exception e) {
            log.warn("远程调用会员服务失败 [会员服务可能未启动]");
        }

        // 2. 远程查询购物车服务
        // feign在远程调用之前要构造请求 调用很多拦截器
        List<OrderItemVo> items = this.cartFeignService.getCurrentUserCartItems();
        confirmVo.setItems(items);

        // 3.查询用户积分
        Integer integration = memberRsepVo.getIntegration();
        confirmVo.setIntegration(integration);
    
        // 4.其他数据在类内部自动计算
        
        // TODO 5.防重令牌
        
        return confirmVo;
    }
    
}