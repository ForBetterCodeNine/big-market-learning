package com.project.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.project.domain.activity.model.entity.DeliverOrderEntity;
import com.project.domain.activity.service.IRaffleActivityAccountQuotaService;
import com.project.domain.credit.model.event.CreditAdjustSuccessMessageEvent;
import com.project.types.enums.ResponseCode;
import com.project.types.event.BaseEvent;
import com.project.types.exception.AppException;
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
 * 积分调整成功消息
 */

@Slf4j
@Component
public class CreditAdjustSuccessCustomer {
    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

    @KafkaListener(topics = "user_credit_adjust_success", groupId = "user_credit_adjust_group", concurrency = "1")
    public void userCreditListener(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> message = Optional.ofNullable(record.value());
        if(message.isPresent()) {
            String msg = (String) message.get();
            try {
                log.info("监听积分账户调整成功消息，进行交易商品发货 topic: {} message: {}", topic, msg);
                BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> eventMessage = JSON.parseObject(msg
                        , new TypeReference<BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage>>() {
                }.getType());
                CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage creditAdjustSuccessMessage = eventMessage.getData();

                DeliverOrderEntity deliverOrderEntity = new DeliverOrderEntity();
                deliverOrderEntity.setUserId(creditAdjustSuccessMessage.getUserId());
                deliverOrderEntity.setOutBusinessNo(creditAdjustSuccessMessage.getOutBusinessNo());
                raffleActivityAccountQuotaService.updateOrder(deliverOrderEntity);
            }catch (AppException e) {
                if (ResponseCode.INDEX_DUP.getCode().equals(e.getCode())) {
                    log.warn("监听积分账户调整成功消息，进行交易商品发货，消费重复 topic: {} message: {}", topic, message, e);
                    return;
                }
                throw e;
            }catch (Exception e) {
                log.error("监听积分账户调整成功消息，进行交易商品发货失败 topic: {} message: {}", topic, message, e);
                throw e;
            }
        }
    }


}
