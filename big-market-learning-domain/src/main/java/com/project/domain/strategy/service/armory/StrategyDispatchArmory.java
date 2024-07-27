package com.project.domain.strategy.service.armory;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.model.entity.StrategyEntity;
import com.project.domain.strategy.model.entity.StrategyRuleEntity;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class StrategyDispatchArmory implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository strategyRepository;


    @Override
    public boolean strategyArmory(Long strategyId) {
        //首先拿到策略对应的奖品实体
        List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardEntityList(strategyId);
        //进行装配
        strategyArmory(String.valueOf(strategyId), strategyAwardEntities);
        //如果对应的策略id 有权重配置 则需要装配对应的权重奖品
        //根据策略id 查找表strategy
        StrategyEntity strategyEntity = strategyRepository.queryStrategyByStrategyId(strategyId);
        if(strategyEntity.ruleModels() == null || strategyEntity.ruleModels().length == 0) return true;
        String ruleWeight = strategyEntity.getRuleWeight();
        if(ruleWeight == null) return true;
        //不为null 则需要进行权重装配
        //根据strategyId和ruleWeight 去查找表strategy_rule 找到对应的积分奖品配置
        StrategyRuleEntity strategyRuleEntity = strategyRepository.queryStrategyRuleByStrategyIdAndRuleWeight(strategyId, ruleWeight);
        if(null == strategyRuleEntity) throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        Map<String, List<Integer>> weightValueMap = strategyRuleEntity.getRuleWeightValues();
        Set<String> keys = weightValueMap.keySet();
        for(String key:keys) {
            //拿到某个积分的奖品列表 4000:101, 102, 103
            List<Integer> ruleWeightValues = weightValueMap.get(key);
            ArrayList<StrategyAwardEntity> strategyRuleEntitiesClone = new ArrayList<>(strategyAwardEntities);
            strategyRuleEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            //进行装配
            strategyArmory(String.valueOf(strategyId).concat("_").concat(key), strategyRuleEntitiesClone);
        }
        return true;
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        //首先需要拿到范围值
        int rateRange = strategyRepository.getRateRangeByStrategyId(strategyId);
        return strategyRepository.getStrategyRandom(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleValue);
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = strategyRepository.getRateRangeByStrategyId(key);
        return strategyRepository.getStrategyRandom(key, new SecureRandom().nextInt(rateRange));
    }

    //用于奖品装配的方法
    private void strategyArmory(String key, List<StrategyAwardEntity> strategyAwardEntities) {
        //获取最小的概率值  以及概率总和
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal sumAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //获取范围值
        BigDecimal rateRange = sumAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        //生成对应的概率查找表
        List<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
        for(StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId = strategyAwardEntity.getAwardId();
            BigDecimal awardRate = strategyAwardEntity.getAwardRate();
            //计算转换之后的奖品个数 并填充
            for(int i=0;i<rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue();i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }

        Collections.shuffle(strategyAwardSearchRateTables);

        //生成map集合 对应的概率值 通过概率获取奖品
        Map<Integer, Integer> shuffleSearchTable = new HashMap<>();
        for(int i=0;i<strategyAwardSearchRateTables.size();i++) {
            shuffleSearchTable.put(i, strategyAwardSearchRateTables.get(i));
        }

        //存放到redis中
        strategyRepository.storeStrategyAwardEntityRateTables(key, shuffleSearchTable.size(), shuffleSearchTable);
    }
}
