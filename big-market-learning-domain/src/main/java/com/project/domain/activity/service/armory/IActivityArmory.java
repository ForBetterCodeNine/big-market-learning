package com.project.domain.activity.service.armory;

/**
 * 进行活动的库存装配
 */
public interface IActivityArmory {
    boolean assembleActivitySku(Long sku);

    boolean assembleActivitySkuByActivityId(Long activityId);
}
