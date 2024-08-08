package com.project.domain.activity.service;

import com.alibaba.fastjson.JSON;
import com.project.domain.activity.model.entity.*;
import com.project.domain.activity.repository.IActivityRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽奖活动抽象类，定义标准的流程
 */

@Slf4j
public class AbstractActivityService implements IRaffleOrder{

    protected IActivityRepository activityRepository;

    public AbstractActivityService(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public ActivityOrderEntity createRaffleActivityOrder(ActivityShopCartEntity activityShopCartEntity) {
        //通过sku查询活动信息
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuEntity(activityShopCartEntity.getSku());
        //查询活动信息
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityEntityByAcId(activitySkuEntity.getActivityId());
        //查询用于可参与次数
        ActivityCountEntity activityCountEntity = activityRepository.queryActivityCountEntityByAccountId(activitySkuEntity.getActivityCountId());

        log.info("查询结果：{} {} {}", JSON.toJSONString(activitySkuEntity), JSON.toJSONString(activityEntity), JSON.toJSONString(activityCountEntity));

        return ActivityOrderEntity.builder().build();
    }
}
