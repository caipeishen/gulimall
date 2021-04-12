package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/12 18:04
 * @Description:
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class HelloSchedule {
    
    /**
     * 定时任务
     * 1、@EnableScheduling开启定时任务
     * 2、@Scheduled开启一个定时任务
     * 3、自动配置类 TaskSchedulingAutoConfiguration
     *
     * 异步任务
     * 1、@EnableAsync开启异步任务功能
     * 2、@Async给希望异步执行的方法上标注
     * 3、自动配置类 TaskExecutionAutoConfiguration 属性绑定在TaskExecutionProperties
     */
    
    @Async
    @Scheduled(cron = "*/5 * 18 * * ?")
    public void hello() throws InterruptedException {
        log.info("hello...");
        Thread.sleep(3000);
    }
    
    
}
