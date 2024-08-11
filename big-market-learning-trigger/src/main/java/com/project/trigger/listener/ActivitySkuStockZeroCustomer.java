package com.project.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.project.domain.activity.service.IRaffleActivitySkuStockService;
import com.project.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * kafka消费者
 */
@Slf4j
@Component
public class ActivitySkuStockZeroCustomer {

    @Resource
    private IRaffleActivitySkuStockService skuStock;


    @KafkaListener(topics = "big-market-activity-sku-stock-consistent", groupId = "big-market-activity-group", concurrency = "1")
    public void activitySkuStockListener(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> message = Optional.ofNullable(record.value());
        if(message.isPresent()) {
            String msg = (String) message.get();
            try {
                log.info("监听活动sku库存消耗为0消息 topic: {} message: {}", topic, message);
                BaseEvent.EventMessage<Long> eventMessage = JSON.parseObject(msg, new TypeReference<BaseEvent.EventMessage<Long>>(){}.getType());
                Long sku = eventMessage.getData();
                //更新库存
                skuStock.clearActivitySkuStock(sku);
                skuStock.clearQueueValue();
                ack.acknowledge();
            }catch (Exception e) {
                log.error("监听活动sku库存消耗为0消息，消费失败 topic: {} message: {}", topic, message);
                throw e;
            }
        }
    }
}
