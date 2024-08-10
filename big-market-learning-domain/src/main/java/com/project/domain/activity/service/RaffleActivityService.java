package com.project.domain.activity.service;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.entity.*;
import com.project.domain.activity.model.valobj.OrderStateVO;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.rule.factory.DefaultActivityChainFactory;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RaffleActivityService extends AbstractActivityService{
    public RaffleActivityService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    @Override
    protected CreateOrderAggregate buildOrderAggregate(SkuRechargeOrderEntity skuRechargeOrderEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        //订单实体对象
        ActivityOrderEntity activityOrderEntity = new ActivityOrderEntity();
        activityOrderEntity.setUserId(skuRechargeOrderEntity.getUserId());
        activityOrderEntity.setSku(skuRechargeOrderEntity.getSku());
        activityOrderEntity.setActivityId(activityEntity.getActivityId());
        activityOrderEntity.setActivityName(activityEntity.getActivityName());
        activityOrderEntity.setStrategyId(activityEntity.getStrategyId());
        activityOrderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        activityOrderEntity.setOrderTime(new Date());
        activityOrderEntity.setTotalCount(activityCountEntity.getTotalCount());
        activityOrderEntity.setDayCount(activityCountEntity.getDayCount());
        activityOrderEntity.setMonthCount(activityCountEntity.getMonthCount());
        activityOrderEntity.setState(OrderStateVO.completed);
        activityOrderEntity.setOutBusinessNo(skuRechargeOrderEntity.getOutBusinessNo());

        // 构建聚合对象
        return CreateOrderAggregate.builder()
                .userId(skuRechargeOrderEntity.getUserId())
                .activityId(activitySkuEntity.getActivityId())
                .totalCount(activityCountEntity.getTotalCount())
                .dayCount(activityCountEntity.getDayCount())
                .monthCount(activityCountEntity.getMonthCount())
                .activityOrderEntity(activityOrderEntity)
                .build();

    }

    @Override
    protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
        activityRepository.doSaveOrder(orderAggregate);
    }
}
