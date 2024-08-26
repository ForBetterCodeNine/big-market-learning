package com.project.domain.credit.service.impl;

import com.project.domain.credit.model.aggregate.TradeAggregate;
import com.project.domain.credit.model.entity.CreditAccountEntity;
import com.project.domain.credit.model.entity.CreditOrderEntity;
import com.project.domain.credit.model.entity.TaskEntity;
import com.project.domain.credit.model.entity.TradeEntity;
import com.project.domain.credit.model.event.CreditAdjustSuccessMessageEvent;
import com.project.domain.credit.repository.ICreditRepository;
import com.project.domain.credit.service.ICreditAdjustService;
import com.project.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class CreditAdjustServiceImpl implements ICreditAdjustService {

    @Resource
    private ICreditRepository creditRepository;

    @Resource
    private CreditAdjustSuccessMessageEvent creditAdjustSuccessMessageEvent;

    @Override
    public String createOrder(TradeEntity tradeEntity) {
        log.info("增加账户积分额度开始 userId:{} tradeName:{} amount:{}", tradeEntity.getUserId(), tradeEntity.getTradeName(), tradeEntity.getAmount());
        //创建账户积分实体
        CreditAccountEntity accountEntity = TradeAggregate.createCreditAccountEntity(
                tradeEntity.getUserId(), tradeEntity.getAmount()
        );

        //创建账户订单实体
        CreditOrderEntity orderEntity = TradeAggregate.createCreditOrderEntity(
                tradeEntity.getUserId(), tradeEntity.getTradeName(), tradeEntity.getTradeType(),
                tradeEntity.getAmount(), tradeEntity.getOutBusinessNo()
        );



        //构建消息任务对象
        CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage creditAdjustSuccessMessage = new CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage();
        creditAdjustSuccessMessage.setUserId(tradeEntity.getUserId());
        creditAdjustSuccessMessage.setOrderId(orderEntity.getOrderId());
        creditAdjustSuccessMessage.setAmount(tradeEntity.getAmount());
        creditAdjustSuccessMessage.setOutBusinessNo(tradeEntity.getOutBusinessNo());
        BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> creditAdjustSuccessMessageEventMessage = creditAdjustSuccessMessageEvent.buildEventMessage(creditAdjustSuccessMessage);

        TaskEntity taskEntity = TradeAggregate.createTaskEntity(
                tradeEntity.getUserId(),
                creditAdjustSuccessMessageEvent.topic(),
                creditAdjustSuccessMessageEventMessage.getId(),
                creditAdjustSuccessMessageEventMessage
        );
        //构建聚合对象
        TradeAggregate tradeAggregate = TradeAggregate.builder()
                .userId(tradeEntity.getUserId())
                .creditAccountEntity(accountEntity)
                .creditOrderEntity(orderEntity)
                .taskEntity(taskEntity)
                .build();



        //保存交易订单
        creditRepository.saveUserCreditTradeOrder(tradeAggregate);
        log.info("增加账户积分额度完成 userId:{} orderId:{}", tradeEntity.getUserId(), orderEntity.getOrderId());


        return orderEntity.getOrderId();
    }
}
