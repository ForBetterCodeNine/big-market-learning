package com.project.domain.activity.service.quota.policy;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
/**
 交易策略接口，包括；返利兑换（不用支付），积分订单（需要支付）
 */
public interface ITradePolicy {
    void trade(CreateOrderAggregate createOrderAggregate);
}
