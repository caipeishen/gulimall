package com.atguigu.gulimall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     *  1. 使用AmqpAdmin创建Exchange、Queue、Binding
     */
    @Test
    void contextLoads() {
    }
    
}
