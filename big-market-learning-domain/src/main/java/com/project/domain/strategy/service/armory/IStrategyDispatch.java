package com.project.domain.strategy.service.armory;

public interface IStrategyDispatch {
    //获取一个随机的奖品 传入strategyId
    Integer getRandomAwardId(Long strategyId);

    //在积分规则下  获取某个奖品
    Integer getRandomAwardId(Long strategyId, String ruleValue);

    Boolean subtractionAwardStock(Long strategyId, Integer awardId);
}
