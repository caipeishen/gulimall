package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.conf.MyMQConfig;
import com.atguigu.gulimall.order.conf.MyRabbitConfig;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/2 17:36
 * @Description:
 */
@RestController
public class RabbitMQController {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 测试发送消息
     * @param num
     * @return
     */
    @GetMapping("/test/sendMQ")
    public String sendMQ(@RequestParam(value = "num", required = false, defaultValue = "10") Integer num){
        OrderEntity entity = new OrderEntity();
        entity.setId(1L);
        entity.setCommentTime(new Date());
        entity.setCreateTime(new Date());
        entity.setConfirmStatus(0);
        entity.setAutoConfirmDay(1);
        entity.setGrowth(1);
        entity.setMemberId(12L);
        
        OrderItemEntity orderEntity = new OrderItemEntity();
        orderEntity.setCategoryId(225L);
        orderEntity.setId(1L);
        orderEntity.setOrderSn("gulimall");
        orderEntity.setSpuName("华为");
        for (int i = 0; i < num; i++) {
            if(i % 2 == 0){
                entity.setReceiverName("Cai Peishen-" + i);
                rabbitTemplate.convertAndSend(MyRabbitConfig.exchange, MyRabbitConfig.routingKey, entity, new CorrelationData(UUID.randomUUID().toString().replace("-","")));
            }else {
                orderEntity.setOrderSn("gulimall-" + i);
                rabbitTemplate.convertAndSend(MyRabbitConfig.exchange, MyRabbitConfig.routingKey, orderEntity, new CorrelationData(UUID.randomUUID().toString().replace("-","")));
                // 测试消息发送失败
//				rabbitTemplate.convertAndSend(MyRabbitConfig.exchange, MyRabbitConfig.routingKey + "test", orderEntity);
            }
        }
        return "ok";
    }
    
    /**
     * 测试发送创建订单
     * @return
     */
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString().replace("-",""));
        orderEntity.setModifyTime(new Date());
        this.rabbitTemplate.convertAndSend(MyMQConfig.eventExchange, MyMQConfig.createOrderRoutingKey, orderEntity);
        return "下单成功";
    }

}
