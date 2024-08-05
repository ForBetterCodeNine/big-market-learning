package com.project.domain.strategy.repository;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.model.entity.StrategyEntity;
import com.project.domain.strategy.model.entity.StrategyRuleEntity;
import com.project.domain.strategy.model.valobj.RuleTreeVO;
import com.project.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.project.domain.strategy.model.valobj.StrategyAwardStockKeyVO;

import java.util.List;
import java.util.Map;

/**
 * 策略配置 仓储服务接口
 */
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardEntityList(Long strategyId);

    StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId);

    void storeStrategyAwardEntityRateTables(String key, Integer rateRange, Map<Integer, Integer> map);

    Integer getRateRangeByStrategyId(Long strategyId);

    Integer getRateRangeByStrategyId(String key);

    Integer getStrategyRandom(String key, Integer rateKey);

    StrategyEntity queryStrategyByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleByStrategyIdAndRuleWeight(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);

    StrategyAwardRuleModelVO queryStrategyRuleModelsByStrategyIdAndAwardId(Long strategyId, Integer awardId);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    void cacheAwardCount(String cacheKey, Integer awardCount);

    boolean subtractionAwardStock(String cacheKey);

    /**
     * 写入奖品库存消费队列
     */
    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    StrategyAwardStockKeyVO takeQueue() throws Exception;

    void updateStrategyAwardStock(Long strategyId, Integer awardId);

}
