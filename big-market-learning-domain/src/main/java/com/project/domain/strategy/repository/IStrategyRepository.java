package com.project.domain.strategy.repository;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.model.entity.StrategyEntity;
import com.project.domain.strategy.model.entity.StrategyRuleEntity;
import com.project.domain.strategy.model.valobj.StrategyAwardRuleModelVO;

import java.util.List;
import java.util.Map;

/**
 * 策略配置 仓储服务接口
 */
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardEntityList(Long strategyId);

    void storeStrategyAwardEntityRateTables(String key, Integer rateRange, Map<Integer, Integer> map);

    Integer getRateRangeByStrategyId(Long strategyId);

    Integer getRateRangeByStrategyId(String key);

    Integer getStrategyRandom(String key, Integer rateKey);

    StrategyEntity queryStrategyByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleByStrategyIdAndRuleWeight(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);

    StrategyAwardRuleModelVO queryStrategyRuleModelsByStrategyIdAndAwardId(Long strategyId, Integer awardId);
}
