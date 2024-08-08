package com.project.domain.activity.repository;

import com.project.domain.activity.model.entity.ActivityCountEntity;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.ActivitySkuEntity;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    //查询sku信息
    ActivitySkuEntity queryActivitySkuEntity(Long sku);

    //查询活动
    ActivityEntity queryRaffleActivityEntityByAcId(Long activityId);

    //查询活动数量信息
    ActivityCountEntity queryActivityCountEntityByAccountId(Long activityCountId);
}
