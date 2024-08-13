package com.project.domain.strategy.service.armory;

/**
 * 策略规则 装配
 */
public interface IStrategyArmory {
    //装配策略对应奖品概率的方法
    boolean strategyArmory(Long strategyId);


    boolean assembleLotteryStrategyByActivity(Long activityId);
}
