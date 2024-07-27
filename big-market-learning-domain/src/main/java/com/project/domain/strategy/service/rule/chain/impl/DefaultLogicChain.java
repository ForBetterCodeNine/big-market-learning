package com.project.domain.strategy.service.rule.chain.impl;

import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.domain.strategy.service.rule.chain.AbstractLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component("default")
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    protected IStrategyDispatch dispatch;


    @Override
    protected String ruleModel() {
        return "default";
    }

    @Override
    public Integer doLogic(Long strategyId, String userId) {
        Integer awardId = dispatch.getRandomAwardId(strategyId);
        log.info("抽奖责任链-默认处理 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
        return awardId;
    }
}
