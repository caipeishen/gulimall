package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRsepVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service("seckillService")
public class SeckillServiceImpl implements SeckillService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private CouponFeignService couponFeignService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    
    private final String SKUSTOCK_SEMAPHONE = "seckill:stock:"; // +商品随机码

    private final String SKILLED_CACHE_PREFIX = "seckill:skilled:"; // 秒杀过的
    
    @Override
    public void uploadSeckillSkuLatest3Day() {
        // 1.扫描最近三天要参加秒杀的商品
        R r = this.couponFeignService.getLate3DaySession();
        if(r.getCode() == 0){
            List<SeckillSessionsWithSkus> sessions = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {});
            // 2.缓存活动信息
            this.saveSessionInfo(sessions);
            // 3.缓存活动的关联的商品信息
            this.saveSessionSkuInfo(sessions);
        }
    }
    
    /**
     * blockHandler：处理限流熔断降级
     * fallback：处理所有异常
     * @return
     */
    @Override
    @SentinelResource(value = "seckillSkusResource", blockHandler = "seckillSkusBlockHandler", fallback = "seckillSkusFallBack")
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // 1.确定当前时间属于那个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = this.stringRedisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            // seckill:sessions:1593993600000_1593995400000
            String replace = key.replace(SESSION_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            long start = Long.parseLong(split[0]);
            long end = Long.parseLong(split[1]);
            if(time >= start && time <= end) {
                // 2.获取这个秒杀场次的所有商品信息
                List<String> range = this.stringRedisTemplate.opsForList().range(key, 0, 1000);
                BoundHashOperations<String, String, String> hashOps = this.stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if(list != null) {
                    return list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
//						redisTo.setRandomCode(null);
                        return redisTo;
                    }).collect(Collectors.toList());
                }
                break;
            }
        }
        return null;
    }
    
    /**
     * blockHandler / blockHandlerClass: blockHandler 对应处理 BlockException 的函数名称，可选项。
     * blockHandler 函数访问范围需要是 public，返回类型需要与原方法相匹配，参数类型需要和原方法相匹配并且最后加一个额外的参数，类型为 BlockException。
     * blockHandler 函数默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以指定 blockHandlerClass 为对应的类的 Class 对象，注意对应的函数必需为 static 函数，否则无法解析
     * @param blockException
     * @return
     */
    public List<SeckillSkuRedisTo> seckillSkusBlockHandler(BlockException blockException) {
        log.info("限流...seckillSkusBlockHandler....");
        return null;
    }
    
    /**
     * fallback / fallbackClass：fallback 函数名称，可选项，用于在抛出异常的时候提供 fallback 处理逻辑。
     * fallback 函数可以针对所有类型的异常（除了 exceptionsToIgnore 里面排除掉的异常类型）进行处理。fallback 函数签名和位置要求：
     *      返回值类型必须与原函数返回值类型一致；
     *      方法参数列表需要和原函数一致，或者可以额外多一个 Throwable 类型的参数用于接收对应的异常。
     *      fallback 函数默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以指定 fallbackClass 为对应的类的 Class 对象，注意对应的函数必需为 static 函数，否则无法解析。
     * @param throwable
     * @return
     */
    public List<SeckillSkuRedisTo> seckillSkusFallBack(Throwable throwable) {
        log.info("异常...seckillSkusFallBack...");
        return null;
    }
    

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = this.stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if(keys != null && keys.size() > 0){
            String regx = "\\d-" + skuId;
            for (String key : keys) {
                if(Pattern.matches(regx, key)){
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo to = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    // 处理一下随机码
                    long current = new Date().getTime();

                    if(current <= to.getStartTime() || current >= to.getEndTime()){
                        to.setRandomCode(null);
                    }
                    return to;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        MemberRsepVo memberRsepVo = LoginInterceptor.loginUser.get();

        // 1.获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = this.stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);
        if(StringUtils.isNotBlank(json)){
            SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            // 校验合法性
            long time = new Date().getTime();
            if(time >= redisTo.getStartTime() && time <= redisTo.getEndTime()) {
                // 1.校验随机码跟商品id是否匹配
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId() + "-" + redisTo.getSkuId();

                if (randomCode.equals(key) && killId.equals(skuId)) {
                    // 2.说明数据合法
                    BigDecimal limit = redisTo.getSeckillLimit();
                    if (num <= limit.intValue()) {
                        // 3.验证这个人是否已经购买过了
                        String redisKey = memberRsepVo.getId() + "-" + skuId;
                        // 让数据自动过期
                        long ttl = redisTo.getEndTime() - redisTo.getStartTime();

                        Boolean aBoolean = this.stringRedisTemplate.opsForValue().setIfAbsent(SKILLED_CACHE_PREFIX + redisKey, num.toString(), ttl < 0 ? 0 : ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            // 占位成功 说明从来没买过
                            RSemaphore semaphore = this.redissonClient.getSemaphore(SKUSTOCK_SEMAPHONE + randomCode);
                            boolean acquire = semaphore.tryAcquire(num);// 使用tryAcquire 不会阻塞
                            if (acquire) {
                                // 秒杀成功
                                // 快速下单 发送MQ
                                String orderSn = IdWorker.getTimeId() + UUID.randomUUID().toString().replace("-", "").substring(7, 8);
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(orderSn);
                                orderTo.setMemberId(memberRsepVo.getId());
                                orderTo.setNum(num);
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                this.rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                return orderSn;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 缓存活动信息
     * @param sessions
     */
    private void saveSessionInfo(List<SeckillSessionsWithSkus> sessions){
        if(sessions != null){
            sessions.stream().forEach(session -> {
                long startTime = session.getStartTime().getTime();
                long endTime = session.getEndTime().getTime();
                String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
                Boolean hasKey = this.stringRedisTemplate.hasKey(key);
                if(!hasKey){
                    // 获取所有商品id
                    List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "-" + item.getSkuId()).collect(Collectors.toList());
                    // 缓存活动信息
                    this.stringRedisTemplate.opsForList().leftPushAll(key, collect);
                }
            });
        }
    }
    
    /**
     * 缓存活动的关联的商品信息
     * @param sessions
     */
    private void saveSessionSkuInfo(List<SeckillSessionsWithSkus> sessions){
        if(sessions != null){
            sessions.stream().forEach(session -> {
                BoundHashOperations<String, Object, Object> ops = this.stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                    if(!ops.hasKey(seckillSkuVo.getPromotionSessionId() + "-" + seckillSkuVo.getSkuId())){
                        // 1.缓存商品
                        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                        BeanUtils.copyProperties(seckillSkuVo, redisTo);
                        // 2.sku的基本数据 sku的秒杀信息
                        R info = this.productFeignService.skuInfo(seckillSkuVo.getSkuId());
                        if(info.getCode() == 0){
                            SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                            redisTo.setSkuInfoVo(skuInfo);
                        }
                        // 3.设置当前商品的秒杀信息
                        redisTo.setStartTime(session.getStartTime().getTime());
                        redisTo.setEndTime(session.getEndTime().getTime());
    
                        // 4.商品的随机码
                        String randomCode = UUID.randomUUID().toString().replace("-", "");
                        redisTo.setRandomCode(randomCode);
                        
                        ops.put(seckillSkuVo.getPromotionSessionId() + "-" + seckillSkuVo.getSkuId(), JSON.toJSONString(redisTo));
                        
                        // 如果当前这个场次的商品库存已经上架就不需要上架了
                        // 5.使用库存作为分布式信号量  限流
                        RSemaphore semaphore = this.redissonClient.getSemaphore(SKUSTOCK_SEMAPHONE + randomCode);
                        semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                    }
                });
            });
        }
    }
    
}
