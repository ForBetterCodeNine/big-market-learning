package com.project.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.project.domain.award.event.SendAwardMessageEvent;
import com.project.domain.award.model.entity.DistributeAwardEntity;
import com.project.domain.award.service.IAwardService;
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

@Slf4j
@Component
public class SendAwardCustomer {
    private static final String topic = "send_award";

    @Resource
    private IAwardService awardService;

    @KafkaListener(topics = "send_award", groupId = "big-market-send-award", concurrency = "1")
    public void activitySendAward(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> message = Optional.ofNullable(record.value());
        if(message.isPresent()) {
            String msg = (String) message.get();
            try {
                log.info("监听用户奖品发送消息 topic: {} message: {}", topic, msg);
                BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> eventMessage = JSON.parseObject(String.valueOf(message), new TypeReference<BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage>>() {
                }.getType());
                SendAwardMessageEvent.SendAwardMessage sendAwardMessage = eventMessage.getData();

                // 发放奖品
                DistributeAwardEntity distributeAwardEntity = new DistributeAwardEntity();
                distributeAwardEntity.setUserId(sendAwardMessage.getUserId());
                distributeAwardEntity.setOrderId(sendAwardMessage.getOrderId());
                distributeAwardEntity.setAwardId(sendAwardMessage.getAwardId());
                distributeAwardEntity.setAwardConfig(sendAwardMessage.getAwardConfig());
                awardService.distributeAward(distributeAwardEntity);
                ack.acknowledge();
            }catch (Exception e) {
                log.error("监听用户奖品发送消息，消费失败 topic: {} message: {}", topic, msg);
                throw e;
            }
        }
    }
}
