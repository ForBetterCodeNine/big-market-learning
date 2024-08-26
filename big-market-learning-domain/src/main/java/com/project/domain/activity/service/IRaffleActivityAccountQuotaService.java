package com.project.domain.activity.service;

import com.project.domain.activity.model.entity.DeliverOrderEntity;
import com.project.domain.activity.model.entity.SkuRechargeOrderEntity;

/**
 * 抽奖活动订单接口
 */
public interface IRaffleActivityAccountQuotaService {
    /**
     * 以sku创建抽奖活动订单，获得参与抽奖资格（可消耗的次数）
     */
    String createSkuRechargeOrder(SkuRechargeOrderEntity skuRechargeOrderEntity);

    /**
     * 查询活动账户日参与次数
     */
    Integer queryRaffleActivityDayPartakeCount(Long activityId, String userId);

    /**
     * 订单出货-积分充值
     */
    void updateOrder(DeliverOrderEntity deliverOrderEntity);
}
