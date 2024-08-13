package com.project.domain.strategy.service.rule;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

/**
 * 查询奖品信息接口
 */
public interface IRaffleAward {
    //根据策略id查询奖品集合
    List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId);

    /**
     * 根据活动ID查询抽奖奖品列表配置
     *
     */
    List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId);
}
