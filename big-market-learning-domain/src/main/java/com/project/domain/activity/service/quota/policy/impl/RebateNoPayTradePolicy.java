package com.project.domain.activity.service.quota.policy.impl;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.valobj.OrderStateVO;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.quota.policy.ITradePolicy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 返利无支付交易订单，直接充值到账
 */

@Service("rebate_no_pay_trade")
public class RebateNoPayTradePolicy implements ITradePolicy {
    private final IActivityRepository activityRepository;

    public RebateNoPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }


    @Override
    public void trade(CreateOrderAggregate createOrderAggregate) {
        createOrderAggregate.setOrderState(OrderStateVO.completed);
        createOrderAggregate.getActivityOrderEntity().setPayAmount(BigDecimal.ZERO);
        activityRepository.doSaveNoPayOrder(createOrderAggregate);
    }
}
