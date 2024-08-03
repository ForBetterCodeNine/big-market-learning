package com.project.domain.strategy.service.rule.chain;

import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;

/**
 * 抽奖前置 责任链
 */
public interface ILogicChain extends ILogicChainArmory{

    //传入strategyId和userId执行logic
    DefaultChainFactory.StrategyAwardVO doLogic(Long strategyId, String userId);
}
