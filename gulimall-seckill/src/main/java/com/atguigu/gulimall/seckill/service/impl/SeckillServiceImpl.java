package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service("secKillService")
public class SeckillServiceImpl implements SeckillService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private CouponFeignService couponFeignService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private ProductFeignService productFeignService;
    
    
    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    
    private final String SKUSTOCK_SEMAPHONE = "seckill:stock:"; // +商品随机码
    
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

    @Override
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
