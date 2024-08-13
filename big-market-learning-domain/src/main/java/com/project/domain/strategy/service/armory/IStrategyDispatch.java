package com.project.domain.strategy.service.armory;

import java.util.Date;

public interface IStrategyDispatch {
    //获取一个随机的奖品 传入strategyId
    Integer getRandomAwardId(Long strategyId);

    //在积分规则下  获取某个奖品
    Integer getRandomAwardId(Long strategyId, String ruleValue);

    Boolean subtractionAwardStock(Long strategyId, Integer awardId);

    /**
     * 根据策略id和奖品id扣减奖品库存
     */
    Boolean subtractionAwardStock(Long strategyId, Integer awardId, Date endDateTime);
}
