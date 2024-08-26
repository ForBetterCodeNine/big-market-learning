package com.project.domain.credit.repository;

import com.project.domain.credit.model.aggregate.TradeAggregate;

public interface ICreditRepository {
    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);
}
