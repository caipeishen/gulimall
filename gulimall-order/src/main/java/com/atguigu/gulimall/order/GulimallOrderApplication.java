package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * 使用RabbitMQ
 * 1、引Amqp场景;RabbitAutoConfiguration就会自动生效
 *      所有的属性都是 spring.rabbitmq
 *      ConfigurationProperties(prefix = "spring.rabbitmq")
 *      public class RabbitProperties{ }
 * 2、给容器中自动配置了 RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 3、@EnableRabbit: @EnableXXX
 */
@EnableRabbit
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallOrderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }
    
}
