package com.project.domain.credit.repository;

import com.project.domain.credit.model.aggregate.TradeAggregate;
import com.project.domain.credit.model.entity.CreditAccountEntity;

public interface ICreditRepository {
    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

    CreditAccountEntity queryUserCreditAccount(String userId);

}
