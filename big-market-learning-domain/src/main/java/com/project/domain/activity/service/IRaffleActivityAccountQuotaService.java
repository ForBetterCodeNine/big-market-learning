package com.project.domain.activity.service;

import com.project.domain.activity.model.entity.SkuRechargeOrderEntity;

/**
 * 抽奖活动订单接口
 */
public interface IRaffleActivityAccountQuotaService {
    /**
     * 以sku创建抽奖活动订单，获得参与抽奖资格（可消耗的次数）
     */
    String createSkuRechargeOrder(SkuRechargeOrderEntity skuRechargeOrderEntity);
}
