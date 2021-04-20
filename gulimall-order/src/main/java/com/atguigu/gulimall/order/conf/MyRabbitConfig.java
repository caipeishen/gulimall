package com.atguigu.gulimall.order.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/23 11:02
 * @Description: 消息队列配置
 */
@Slf4j
@Configuration
public class MyRabbitConfig {
    
    // 测试用
    public static final String exchange = "hello-java-exchange";
    public static final String queue = "hello-java-queue";
    public static final String routingKey = "hello.java";
    
//    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }
    
    @Bean
    public DirectExchange helloJavaExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new DirectExchange(MyRabbitConfig.exchange, true, false);
    }
    
    @Bean
    public Queue helloJavaQueue() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new Queue(MyRabbitConfig.queue, true, false, false);
    }
    
    @Bean
    public Binding helloJavaBinding() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new Binding(MyRabbitConfig.queue,
                Binding.DestinationType.QUEUE,
                MyRabbitConfig.exchange,
                MyRabbitConfig.routingKey,
                null);
    }
    
    
    /**
     * 序列化配置
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * 定制RabbitTemplate
     * 1.消息正确抵达交换机进行回调
     *      1.spring.rabbitmq.publisher-confirms=true
     *      2.设置确认回调(设置消息抵达exchange回调) confirmCallback
     *
     * 2.消息正确抵达队列进行回调
     *      1. spring.rabbitmq.publisher-returns=true、spring.rabbitmq.template.mandatory=true
     *      2.设置返回回调(设置消息抵达queue回调) returnCallback
     *
     * 3.消费端确认（保证每个消息被正确消费，此时才可以broker删除这个消息）
     *      1.spring.rabbitmq.listener.simple.acknowledge-mode=manual
     *		2.默认是自动确认的 只要消息接收到 服务端就会移除这个消息
     *		3.如何签收:
     *			签收: channel.basicAck(deliveryTag, false);
     *			拒签: channel.basicNack(deliveryTag, false,true);
     */
//    @PostConstruct // MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {
        // 设置消息抵达exchange回调
        this.rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达Broker就ack=true
             * @param correlationData 当前消息的唯一关联（唯一id）
             * @param ack 消息是否收到
             * @param cause 失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("confirm...correlationData[{}]==>ack[{}]==>cause[{}]", correlationData, ack, cause);
            }
        });
    
        // 设置消息抵达queue回调
        this.rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列,就触发这个失败回调
             * @param message 投递失败的消息详细信息
             * @param replyCode 回复状态吗
             * @param replyText 回复文本内容
             * @param exchange 当时消息发给哪个交换机
             * @param routingKey 当时消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("Fail...message[{}]==>replyCode[{}]==>replyText[{}]==>exchange[{}]==>routingKey[{}]", message, replyCode, replyText, exchange, routingKey);
            }
        });
        
    }
    
}
