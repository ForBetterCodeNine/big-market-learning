package com.project.domain.strategy.repository;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;
import java.util.Map;

/**
 * 策略配置 仓储服务接口
 */
public interface IStrategyRepository {
    List<StrategyAwardEntity> queryStrategyAwardEntityList(Long strategyId);

    void storeStrategyAwardEntityRateTables(Long strategyId, Integer rateRange, Map<Integer, Integer> map);

    Integer getRateRangeByStrategyId(Long strategyId);

    Integer getStrategyRandom(Long strategyId, Integer rateKey);
}
