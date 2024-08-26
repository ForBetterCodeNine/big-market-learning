package com.project.domain.credit.service.impl;

import com.project.domain.credit.model.aggregate.TradeAggregate;
import com.project.domain.credit.model.entity.CreditAccountEntity;
import com.project.domain.credit.model.entity.CreditOrderEntity;
import com.project.domain.credit.model.entity.TradeEntity;
import com.project.domain.credit.repository.ICreditRepository;
import com.project.domain.credit.service.ICreditAdjustService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class CreditAdjustServiceImpl implements ICreditAdjustService {

    @Resource
    private ICreditRepository creditRepository;

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

        //构建聚合对象
        TradeAggregate tradeAggregate = TradeAggregate.builder()
                .userId(tradeEntity.getUserId())
                .creditAccountEntity(accountEntity)
                .creditOrderEntity(orderEntity)
                .build();

        //保存交易订单
        creditRepository.saveUserCreditTradeOrder(tradeAggregate);
        log.info("增加账户积分额度完成 userId:{} orderId:{}", tradeEntity.getUserId(), orderEntity.getOrderId());


        return orderEntity.getOrderId();
    }
}
