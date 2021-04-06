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
    
    public static final String eventExchange = "order-event-exchange";
    
    
    public static final String createOrderRoutingKey = "order.create.order";
    
    public static final String delayQueue = "order.delay.queue";
    
    
    public static final String releaseRoutingKey = "order.release.order";
    
    public static final String releaseQueue = "order.release.order.queue";
    
    
    public static final String ttl = "900000";
    
    
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange(eventExchange, true, false);
    }
    

    @Bean
    public Queue orderDelayQueue(){
        Map<String ,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", eventExchange);
        arguments.put("x-dead-letter-routing-key", releaseRoutingKey);
        arguments.put("x-message-ttl", ttl);
        Queue queue = new Queue(delayQueue, true, false, false, arguments);
        return queue;
    }
    
    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue(releaseQueue, true, false, false);
        return queue;
    }
    

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding(delayQueue, Binding.DestinationType.QUEUE, eventExchange, createOrderRoutingKey, null);
    }
    
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding(releaseQueue, Binding.DestinationType.QUEUE, eventExchange, releaseRoutingKey, null);
    }
    
}
