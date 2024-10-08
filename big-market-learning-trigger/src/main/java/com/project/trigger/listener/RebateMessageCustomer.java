package com.project.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.project.domain.activity.model.entity.SkuRechargeOrderEntity;
import com.project.domain.activity.model.valobj.OrderTradeTypeVO;
import com.project.domain.activity.service.IRaffleActivityAccountQuotaService;
import com.project.domain.credit.model.entity.TradeEntity;
import com.project.domain.credit.model.valobj.TradeNameVO;
import com.project.domain.credit.model.valobj.TradeTypeVO;
import com.project.domain.credit.service.ICreditAdjustService;
import com.project.domain.rebate.event.SendRebateMessageEvent;
import com.project.domain.rebate.service.IBehaviorRebateService;
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
import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component()
public class RebateMessageCustomer {

    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

    @Resource
    private IBehaviorRebateService rebateService;

    @Resource
    private ICreditAdjustService creditAdjustService;

    @KafkaListener(topics = "big-market-daily-rebate-topic", groupId = "big-market-daily-rebate-group", concurrency = "1")
    public void rebate_listener(ConsumerRecord<?,?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> msg = Optional.ofNullable(record.value());
        String message = (String)msg.get();
        try {
            log.info("监听用户行为返利消息 topic: {} message: {}", topic, message);
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> eventMessage = JSON.parseObject(
                    message,new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>(){}.getType()
            );
            SendRebateMessageEvent.RebateMessage rebateMessage = eventMessage.getData();
            switch (rebateMessage.getRebateType()) {
                case "sku":
                    SkuRechargeOrderEntity skuRechargeOrderEntity = new SkuRechargeOrderEntity();
                    skuRechargeOrderEntity.setUserId(rebateMessage.getUserId());
                    skuRechargeOrderEntity.setOutBusinessNo(rebateMessage.getBizId());
                    skuRechargeOrderEntity.setSku(Long.valueOf(rebateMessage.getRebateConfig()));
                    skuRechargeOrderEntity.setOrderTradeTypeVO(OrderTradeTypeVO.rebate_no_pay_trade);
                    raffleActivityAccountQuotaService.createSkuRechargeOrder(skuRechargeOrderEntity);
                    break;
                case "integral":
                    TradeEntity tradeEntity = new TradeEntity();
                    tradeEntity.setUserId(rebateMessage.getUserId());
                    tradeEntity.setTradeName(TradeNameVO.REBATE);
                    tradeEntity.setTradeType(TradeTypeVO.FORWARD);
                    tradeEntity.setAmount(new BigDecimal(rebateMessage.getRebateConfig()));
                    tradeEntity.setOutBusinessNo(rebateMessage.getBizId());
                    creditAdjustService.createOrder(tradeEntity);
                    break;
            }



            ack.acknowledge();
        }catch (AppException e) {
            if (ResponseCode.INDEX_DUP.getCode().equals(e.getCode())) {
                log.warn("监听用户行为返利消息，消费重复 topic: {} message: {}", topic, message, e);
                return;
            }
            throw e;
        }catch (Exception e) {
            log.error("监听用户行为返利消息，消费失败 topic: {} message: {}", topic, message, e);
            throw e;
        }
    }
}
