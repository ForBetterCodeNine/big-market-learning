package com.project.domain.strategy.service.armory;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class StrategyArmory implements IStrategyArmory {

    @Resource
    private IStrategyRepository strategyRepository;


    @Override
    public boolean strategyArmory(Long strategyId) {
        //首先拿到策略对应的奖品实体
        List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardEntityList(strategyId);

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
        strategyRepository.storeStrategyAwardEntityRateTables(strategyId, shuffleSearchTable.size(), shuffleSearchTable);
        return true;
    }

    @Override
    public Integer getRandomAwardId(Long strategyId) {
        //首先需要拿到范围值
        int rateRange = strategyRepository.getRateRangeByStrategyId(strategyId);
        return strategyRepository.getStrategyRandom(strategyId, new SecureRandom().nextInt(rateRange));
    }
}
