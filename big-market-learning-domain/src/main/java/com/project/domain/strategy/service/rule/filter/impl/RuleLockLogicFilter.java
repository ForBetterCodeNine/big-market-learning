package com.project.domain.strategy.service.rule.filter.impl;

import com.project.domain.strategy.model.entity.RuleActionEntity;
import com.project.domain.strategy.model.entity.RuleMatterEntity;
import com.project.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.annotation.LogicStrategy;
import com.project.domain.strategy.service.rule.filter.ILogicFilter;
import com.project.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@LogicStrategy(logicModel = DefaultLogicFactory.LogicModel.RULE_LOCK)
public class RuleLockLogicFilter implements ILogicFilter<RuleActionEntity.RaffleCenterEntity> {

    @Resource
    private IStrategyRepository repository;

    //用户抽奖次数
    private Long userRaffleCount = 0L;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleCenterEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-次数锁，userId = {}, strategyId = {}, ruleModel = {}", ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());
        //查询规则配置
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
        long count = Long.parseLong(ruleValue);
        //满足条件则allow
        if(userRaffleCount >= count) {
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }
        // 用户抽奖次数小于规则限定值，规则拦截
        return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                .build();
    }
}
