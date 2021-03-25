package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 使用RabbitMQ
 * 1、引Amqp场景;RabbitAutoConfiguration就会自动生效
 *      所有的属性都是 spring.rabbitmq
 *      ConfigurationProperties(prefix = "spring.rabbitmq")
 *      public class RabbitProperties{ }
 * 2、给容器中自动配置了 RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 3、@EnableRabbit: @EnableXXX
 *
 * Seata控制分布式事务
 * 1)、每一个微服务先必须创建undo_log;
 * 2)、安装事务协调器; seata-server:https://github.com/seata/seata/releases
 * 3)、整合
 *      1、导入依赖spring-cLoud-starter-alibaba-seata seata-all-0.7.1
 *      2、解压并启动seata-server;
 *          registry.conf:注册中心配置; type=nacos; nacos的注册地址
 *          file.conf:配置中心; store=db；seata的事务控制配置地址
 *      3、所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己的数据源
 *
 *      4、每个微服务，都必须导入
 *          registry.conf
 *          file.conf     service模块：vgroup_mapping.{application.name}-fescar-service-group = "default"
 *      5、给分布式大事务的入口标注@GLobaLTransactional
 *      7、每一个远程的小事务用@Transactional
 *
 */
@EnableFeignClients
@EnableRedisHttpSession
@EnableRabbit
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallOrderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }
    
}
