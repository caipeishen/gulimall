package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRsepVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import com.atguigu.gulimall.order.vo.SkuStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


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

    @Autowired
    private ThreadPoolExecutor executor;
    
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

        // 这一步至关重要 冲主线程获取用户数据 异步线程来共享
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        // 1.远程查询所有的收获地址列表
        CompletableFuture addressFuture = CompletableFuture.runAsync(() -> {
            // 异步线程共享
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddressVo> address = this.memberFeignService.getAddress(memberRsepVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        // 2. 远程查询购物车服务
        CompletableFuture cartFuture = CompletableFuture.runAsync(() -> {
            // 异步线程共享
            RequestContextHolder.setRequestAttributes(attributes);
            // feign在远程调用之前要构造请求 调用很多拦截器
            List<OrderItemVo> items = this.cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(()->{
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = confirmVo.getItems();
            // 获取所有商品的id
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = this.wmsFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {});
            if(data != null){
                // 各个商品id 与 他们库存状态的映射
                Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(stocks);
            }
        },executor);;

        // 3.查询用户积分
        Integer integration = memberRsepVo.getIntegration();
        confirmVo.setIntegration(integration);
    
        // 4.其他数据在类内部自动计算
        // TODO 5.防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRsepVo.getId(), token, 10, TimeUnit.MINUTES);
        CompletableFuture.allOf(addressFuture, cartFuture).get();
        
        return confirmVo;
    }
    
}