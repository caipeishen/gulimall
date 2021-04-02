package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.conf.MyMQConfig;
import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/2 17:36
 * @Description:
 */
@Controller
public class HelloController {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 监听过期的订单
     * @param orderEntity
     */
    @RabbitListener(queues = MyMQConfig.releaseQueue)
    public void listener(OrderEntity orderEntity) {
        System.out.println("收到过期的订单信息:准备关闭订单" + orderEntity.getOrderSn());
    }
    
    /**
     * 测试发送创建订单
     * @return
     */
    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString().replace("-",""));
        orderEntity.setModifyTime(new Date());
        this.rabbitTemplate.convertAndSend(MyMQConfig.eventExchange, MyMQConfig.createOrderRoutingKey, orderEntity);
        return "下单成功";
    }

}
