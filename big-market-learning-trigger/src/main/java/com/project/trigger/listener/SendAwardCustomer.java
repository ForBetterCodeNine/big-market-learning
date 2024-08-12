package com.project.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class SendAwardCustomer {
    private static final String topic = "send_award";

    @KafkaListener(topics = "send_award", groupId = "big-market-send-award", concurrency = "1")
    public void activitySendAward(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> message = Optional.ofNullable(record.value());
        if(message.isPresent()) {
            String msg = (String) message.get();
            try {
                log.info("监听用户奖品发送消息 topic: {} message: {}", topic, msg);
                ack.acknowledge();
            }catch (Exception e) {
                log.error("监听用户奖品发送消息，消费失败 topic: {} message: {}", topic, msg);
                throw e;
            }
        }
    }
}
