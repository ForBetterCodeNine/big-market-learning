package com.project.trigger.api;

import com.project.trigger.api.dto.ActivityDrawRequestDTO;
import com.project.trigger.api.dto.ActivityDrawResponseDTO;
import com.project.trigger.api.dto.SkuProductResponseDTO;
import com.project.trigger.api.dto.SkuProductShopCartRequestDTO;
import com.project.types.model.Response;

import java.math.BigDecimal;
import java.util.List;

/**
 * 抽奖活动服务
 */
public interface IRaffleActivityService {
    /**
     * 活动装配 数据预热缓存
     */
    Response<Boolean> armory(Long activityId);

    /**
     * 活动抽奖接口
     */
    Response<ActivityDrawResponseDTO> draw(ActivityDrawRequestDTO requestDTO);

    /**
     * 用户签到返利接口
     */
    Response<Boolean> calendarSignRebate(String userId);

    /**
     * 查询SKU商品集合
     */
    Response<List<SkuProductResponseDTO>> querySkuProductListByActivityId(Long activityId);

    /**
     * 查询用户积分值
     */
    Response<BigDecimal> queryUserCreditAccount(String userId);

    /**
     * 积分支付兑换商品
     */
    Response<Boolean> creditPayExchangeSku(SkuProductShopCartRequestDTO requestDTO);
}
