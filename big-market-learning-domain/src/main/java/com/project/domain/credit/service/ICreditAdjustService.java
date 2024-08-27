package com.project.domain.credit.service;

import com.project.domain.credit.model.entity.CreditAccountEntity;
import com.project.domain.credit.model.entity.TradeEntity;

public interface ICreditAdjustService {
    /**
     * 创建增加积分额度订单
     */
    String createOrder(TradeEntity tradeEntity);

    CreditAccountEntity queryUserCreditAccount(String userId);
}
