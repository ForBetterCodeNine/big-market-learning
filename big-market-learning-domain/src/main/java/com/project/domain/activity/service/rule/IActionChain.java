package com.project.domain.activity.service.rule;

import com.project.domain.activity.model.entity.ActivityCountEntity;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.ActivitySkuEntity;

/**
 * 下单规则过滤接口 责任链模式
 */
public interface IActionChain extends IActionArmoryChain {
    boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);
}
