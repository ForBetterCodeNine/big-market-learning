package com.project.infrastructure.persistent.repository;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.model.entity.StrategyEntity;
import com.project.domain.strategy.model.entity.StrategyRuleEntity;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.infrastructure.persistent.dao.IStrategyAwardDao;
import com.project.infrastructure.persistent.dao.IStrategyDao;
import com.project.infrastructure.persistent.dao.IStrategyRuleDao;
import com.project.infrastructure.persistent.po.Strategy;
import com.project.infrastructure.persistent.po.StrategyAward;
import com.project.infrastructure.persistent.po.StrategyRule;
import com.project.infrastructure.persistent.redis.IRedisService;
import com.project.types.common.Constants;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private IStrategyDao strategyDao;

    @Resource
    private IStrategyRuleDao strategyRuleDao;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardEntityList(Long strategyId) {
        //查到某个策略id下的所有奖品信息
        //先从缓存中获取， 缓存中没有 先保存到数据库 再插入到缓存
        String key = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntitieList = redisService.getValue(key);
        if(null != strategyAwardEntitieList && !strategyAwardEntitieList.isEmpty()) return strategyAwardEntitieList;
        //从数据库中获取
        List<StrategyAward> strategyAwardList = strategyAwardDao.queryStrategyAwardEntityListByStrategyId(strategyId);
        strategyAwardEntitieList = new ArrayList<>(strategyAwardList.size());
        for(StrategyAward strategyAward : strategyAwardList) {
            StrategyAwardEntity strategyAwardEntity = new StrategyAwardEntity();
            strategyAwardEntity.setStrategyId(strategyId);
            strategyAwardEntity.setAwardId(strategyAward.getAwardId());
            strategyAwardEntity.setAwardCount(strategyAward.getAwardCount());
            strategyAwardEntity.setAwardCountSurplus(strategyAward.getAwardCountSurplus());
            strategyAwardEntity.setAwardRate(strategyAward.getAwardRate());
            strategyAwardEntitieList.add(strategyAwardEntity);
        }
        redisService.setValue(key, strategyAwardEntitieList);
        return strategyAwardEntitieList;
    }

    @Override
    public void storeStrategyAwardEntityRateTables(String key, Integer rateRange, Map<Integer, Integer> map) {
        //把某个策略下 对应的奖品转换后的下标信息 以及随机数的范围 存放到redis中
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, rateRange);
        Map<Integer, Integer> searchTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        searchTable.putAll(map);
    }

    @Override
    public Integer getRateRangeByStrategyId(Long strategyId) {
        return getRateRangeByStrategyId(String.valueOf(strategyId));
    }

    @Override
    public Integer getRateRangeByStrategyId(String key) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }


    @Override
    public Integer getStrategyRandom(String key, Integer rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key, rateKey);
    }

    @Override
    public StrategyEntity queryStrategyByStrategyId(Long strategyId) {
        String cacheKey = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if(strategyEntity != null) return strategyEntity;
        strategyEntity = new StrategyEntity();
        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        strategyEntity.setStrategyId(strategy.getStrategyId());
        strategyEntity.setRuleModels(strategy.getRuleModels());
        strategyEntity.setStrategyDesc(strategy.getStrategyDesc());
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRuleByStrategyIdAndRuleWeight(Long strategyId, String ruleModel) {
        String cacheKey = Constants.RedisKey.STRATEGY_RULE_KEY + strategyId;
        StrategyRuleEntity strategyRuleEntity = redisService.getValue(cacheKey);
        if(strategyRuleEntity != null) return strategyRuleEntity;
        StrategyRule strategyRule = strategyRuleDao.queryStrategyRuleByStrategyIdAndRuleWeight(strategyId, ruleModel);
        strategyRuleEntity = new StrategyRuleEntity();
        strategyRuleEntity.setStrategyId(strategyRule.getStrategyId());
        strategyRuleEntity.setRuleValue(strategyRule.getRuleValue());
        strategyRuleEntity.setRuleType(strategyRule.getRuleType());
        strategyRuleEntity.setAwardId(strategyRule.getAwardId());
        strategyRuleEntity.setRuleDesc(strategyRule.getRuleDesc());
        strategyRuleEntity.setRuleModel(strategyRule.getRuleModel());
        redisService.setValue(cacheKey, strategyRuleEntity);
        return strategyRuleEntity;
    }
}
