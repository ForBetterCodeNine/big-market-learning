package com.project.domain.strategy.service.rule;

import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;

/**
 * 执行抽奖的接口
 */
public interface IRaffleStrategy {
    /**
     * 用抽奖因子入参 执行抽奖计算 返回奖品信息
     */
    RaffleAwardEntity performRaffle(RaffleFactorEntity factorEntity);
}
