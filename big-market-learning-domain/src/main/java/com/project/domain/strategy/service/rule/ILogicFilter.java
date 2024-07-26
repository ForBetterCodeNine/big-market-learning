package com.project.domain.strategy.service.rule;


import com.project.domain.strategy.model.entity.RuleActionEntity;
import com.project.domain.strategy.model.entity.RuleMatterEntity;

/**
 * 抽奖规则过滤接口 返回被哪个过滤器拦截的实体对象
 *
 */
public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity>{

    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);
}
