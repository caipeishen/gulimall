package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/12 18:04
 * @Description: 秒杀商品定时上架		[秒杀的定时任务调度]
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

	@Autowired
	private SeckillService seckillService;

	@Autowired
	private RedissonClient redissonClient;

	private final String upload_lock = "seckill:upload:lock";
	
	/**
	 * 这里应该是幂等的
	 *  三秒执行一次：* /3 * * * * ?
	 */
	@Async
	@Scheduled(cron = "0/3 * * * * ?")
	public void uploadSeckillSkuLatest3Day(){
		log.info("上架秒杀商品的信息");
		// 1.重复上架无需处理 加上分布式锁 状态已经更新 释放锁以后其他人才获取到最新状态
		RLock lock = this.redissonClient.getLock(upload_lock);
		lock.lock(10, TimeUnit.SECONDS);
		try {
			this.seckillService.uploadSeckillSkuLatest3Day();
		} finally {
			lock.unlock();
		}
	}
}
