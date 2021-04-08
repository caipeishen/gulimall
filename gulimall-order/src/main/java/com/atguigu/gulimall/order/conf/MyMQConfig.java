package com.atguigu.gulimall.order.conf;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/2 15:18
 * @Description:
 */
@Configuration
public class MyMQConfig {

    
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange", true, false);
    }
    
    
    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        Map<String ,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 30000); // 30秒 过期时间 毫秒单位 一定要是整数类型
        return new Queue("order.delay.queue", true, false, false, arguments);
    }
    
    /**
     * 普通队列，用于解锁订单
     * @return
     */
    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.order.queue", true, false, false);
    }
    
    
    /**
     * 交换机和延迟队列绑定
     * @return
     */
    @Bean
    public Binding orderCreateBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }
    
    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding orderReleaseBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }

    /**
     * 订单交换机和释放库存队列绑定
     * @return
     */
    @Bean
    public Binding orderReleaseStockBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.other.#", null);
    }

}
