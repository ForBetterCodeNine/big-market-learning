package com.project.domain.activity.service;

import com.project.domain.activity.model.entity.ActivityCountEntity;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.ActivitySkuEntity;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.rule.factory.DefaultActivityChainFactory;

/**
 * 抽奖活动的支撑类 用于实现对数据库的操作后数据封装
 */

public class RaffleActivitySupport {

    protected DefaultActivityChainFactory defaultActivityChainFactory;

    protected IActivityRepository activityRepository;

    public RaffleActivitySupport(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        this.defaultActivityChainFactory = defaultActivityChainFactory;
        this.activityRepository = activityRepository;
    }

    public ActivitySkuEntity queryActivitySku(Long sku) {
        return activityRepository.queryActivitySkuEntity(sku);
    }

    public ActivityEntity queryActivityEntity(Long activityId) {
        return activityRepository.queryRaffleActivityEntityByAcId(activityId);
    }

    public ActivityCountEntity queryActivityCountEntity(Long activityCountId) {
        return activityRepository.queryActivityCountEntityByAccountId(activityCountId);
    }
}
