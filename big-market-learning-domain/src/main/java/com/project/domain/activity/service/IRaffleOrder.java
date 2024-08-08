package com.project.domain.activity.service;

import com.project.domain.activity.model.entity.ActivityOrderEntity;
import com.project.domain.activity.model.entity.ActivityShopCartEntity;

/**
 * 抽奖活动订单接口
 */
public interface IRaffleOrder {
    /**
     * 以sku创建抽奖活动订单，获得参与抽奖资格（可消耗的次数）
     */
    ActivityOrderEntity createRaffleActivityOrder(ActivityShopCartEntity activityShopCartEntity);
}
