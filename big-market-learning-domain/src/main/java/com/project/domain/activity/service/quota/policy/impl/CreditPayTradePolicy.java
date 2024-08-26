package com.project.domain.activity.service.quota.policy.impl;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.valobj.OrderStateVO;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.quota.policy.ITradePolicy;
import org.springframework.stereotype.Service;

/**
 * 积分兑换，支付类订单
 */

@Service("credit_pay_trade")
public class CreditPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public CreditPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateOrderAggregate createOrderAggregate) {
        createOrderAggregate.setOrderState(OrderStateVO.wait_pay);
        activityRepository.doSaveCreditPayOrder(createOrderAggregate);
    }
}
