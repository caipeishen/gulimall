package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = { "stock.release.stock.queue" })
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("************************收到库存解锁的消息********************************");
        try {
            this.wareSkuService.unlock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
