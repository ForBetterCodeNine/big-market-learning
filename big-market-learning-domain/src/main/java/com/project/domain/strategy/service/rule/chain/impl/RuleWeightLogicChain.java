package com.project.domain.strategy.service.rule.chain.impl;

import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.project.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository repository;

    @Resource
    protected IStrategyDispatch dispatch;

    public Long userScore = 0L;

    @Override
    protected String ruleModel() {
        return DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode();
    }

    /**
     * 权重责任链过滤；
     * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO doLogic(Long strategyId, String userId) {
        log.info("抽奖责任链-权重开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        String ruleValue = repository.queryStrategyRuleValue(strategyId, null, ruleModel());
        Map<Long, String> ruleValueMap = getAnalyticalValue(ruleValue);
        if(ruleValueMap == null || ruleValueMap.isEmpty()) {
            log.warn("抽奖责任链-权重告警【策略配置权重，但ruleValue未配置相应值】 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
            return next().doLogic(strategyId, userId);
        }

        List<Long> keys = new ArrayList<>(ruleValueMap.keySet());
        Collections.sort(keys);
        Long nextValue = null;
        for(int i=0;i<keys.size();i++) {
            if(userScore >= keys.get(i)) {
                nextValue = keys.get(i);
                break;
            }
        }
        if(nextValue != null) {
            Integer randomAwardId = dispatch.getRandomAwardId(strategyId, ruleValueMap.get(nextValue));
            log.info("抽奖责任链-权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), randomAwardId);
            return DefaultChainFactory.StrategyAwardVO.builder()
                    .awardId(randomAwardId)
                    .logicModel(ruleModel())
                    .build();
        }

        //  过滤其他责任链
        log.info("抽奖责任链-权重放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        return next().doLogic(strategyId, userId);
    }


    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            // 检查输入是否为空
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }
            // 分割字符串以获取键和值
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }
}
