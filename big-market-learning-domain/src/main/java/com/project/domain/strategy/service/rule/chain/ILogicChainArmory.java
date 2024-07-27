package com.project.domain.strategy.service.rule.chain;

/**
 * 进行装配
 */
public interface ILogicChainArmory {
    //获得责任链的下一个节点
    ILogicChain next();

    //添加责任链节点
    ILogicChain appendNext(ILogicChain next);
}
