package com.project.domain.strategy.service.raffle;

import com.project.domain.strategy.model.valobj.RuleTreeVO;
import com.project.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.domain.strategy.service.rule.AbstractRaffleStrategy;
import com.project.domain.strategy.service.rule.chain.ILogicChain;
import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.project.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.project.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    public DefaultRaffleStrategy(DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory, IStrategyDispatch strategyDispatch, IStrategyRepository strategyRepository) {
        super(defaultChainFactory, defaultTreeFactory, strategyDispatch, strategyRepository);
    }

    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(Long strategyId, String userId) {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        return logicChain.doLogic(strategyId, userId);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId) {
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyRuleModelsByStrategyIdAndAwardId(strategyId, awardId);
        if(null == strategyAwardRuleModelVO) {
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if (null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
        }
        IDecisionTreeEngine decisionTreeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        return decisionTreeEngine.process(userId, strategyId, awardId);
    }


}
