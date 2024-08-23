package com.project.domain.strategy.service.rule.chain.impl;

import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.project.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component("rule_blacklist")
public class RuleBlackLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository repository;


    @Override
    protected String ruleModel() {
        return DefaultChainFactory.LogicModel.RULE_BLACKLIST.getCode();
    }

    @Override
    public DefaultChainFactory.StrategyAwardVO doLogic(Long strategyId, String userId) {
        log.info("抽奖责任链-黑名单开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        //查询规则配置
        String ruleValue = repository.queryStrategyRuleValue(strategyId, null, ruleModel());
        String[] splitValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.parseInt(splitValue[0]);
        String[] blackIdList = splitValue[1].split(Constants.SPLIT);
        for(String id:blackIdList) {
            if(userId.equals(id)) {
                log.info("抽奖责任链-黑名单接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
                return DefaultChainFactory.StrategyAwardVO.builder()
                        .awardId(awardId)
                        .logicModel(ruleModel())
                        .awardRuleValue("0.01,1")
                        .build();
            }

        }
        // 过滤其他责任链
        log.info("抽奖责任链-黑名单放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        return next().doLogic(strategyId, userId);
    }
}
