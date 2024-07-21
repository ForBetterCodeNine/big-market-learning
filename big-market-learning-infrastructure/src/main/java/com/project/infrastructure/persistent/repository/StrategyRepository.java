package com.project.infrastructure.persistent.repository;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.infrastructure.persistent.dao.IStrategyAwardDao;
import com.project.infrastructure.persistent.po.StrategyAward;
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
    public void storeStrategyAwardEntityRateTables(Long strategyId, Integer rateRange, Map<Integer, Integer> map) {
        //把某个策略下 对应的奖品转换后的下标信息 以及随机数的范围 存放到redis中
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId, rateRange);
        Map<Integer, Integer> searchTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId);
        searchTable.putAll(map);
    }

    @Override
    public Integer getRateRangeByStrategyId(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }

    @Override
    public Integer getStrategyRandom(Long strategyId, Integer rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, rateKey);
    }
}
