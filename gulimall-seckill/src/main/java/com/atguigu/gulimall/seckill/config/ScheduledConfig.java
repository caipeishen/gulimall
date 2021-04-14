package com.atguigu.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/13 17:40
 * @Description:
 */
@EnableAsync //开启对异步的支持，防止定时任务之间相互阻塞
@EnableScheduling //开启对定时任务的支持
@Configuration
public class ScheduledConfig {
}
