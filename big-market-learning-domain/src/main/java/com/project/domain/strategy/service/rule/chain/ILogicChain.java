package com.project.domain.strategy.service.rule.chain;

/**
 * 抽奖前置 责任链
 */
public interface ILogicChain extends ILogicChainArmory{

    //传入strategyId和userId执行logic
    Integer doLogic(Long strategyId, String userId);
}
