package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.conf.MyRabbitConfig;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;


/**
 * @author Cai Peishen
 * @email peishen.cai@foxmail.com
 * @date 2021-03-23 10:59:19
 */
@RestController
public class RabbitController {

	@Autowired
	private RabbitTemplate rabbitTemplate;


	@GetMapping("/sendMQ")
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
}
