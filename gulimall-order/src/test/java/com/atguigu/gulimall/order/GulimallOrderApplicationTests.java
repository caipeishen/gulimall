package com.atguigu.gulimall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     *  1. 使用AmqpAdmin创建Exchange、Queue、Binding
     */


    @Test
    void createExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        DirectExchange exchange = new DirectExchange("hello-java-exchange", true, false);
        this.amqpAdmin.declareExchange(exchange);
        log.info("Exchange [" + exchange.getName() + "] 创建成功");
    }

    @Test
    void createQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        Queue queue = new Queue("hello-java-queue", true, false, false);
        this.amqpAdmin.declareQueue(queue);
        log.info("Queue [" + queue.getName() + "] 创建成功");
    }

    @Test
    void createBinding() {
        // String destination[目的地], Binding.DestinationType destinationType[目的地类型], String exchange[交换机名称], String routingKey[路由键], @Nullable Map<String, Object> arguments[参数]
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);
        this.amqpAdmin.declareBinding(binding);
        log.info("Binding [" + "hello-java-binding" + "] 创建成功");
    }
}
