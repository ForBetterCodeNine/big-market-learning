package com.project.domain.strategy.service.rule.chain.factory;

import com.project.domain.strategy.model.entity.StrategyEntity;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.rule.chain.ILogicChain;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultChainFactory {
    private final Map<String, ILogicChain> logicChainMap;

    protected IStrategyRepository repository;

    public DefaultChainFactory(Map<String, ILogicChain> logicChainMap, IStrategyRepository repository) {
        this.logicChainMap = logicChainMap;
        this.repository = repository;
    }

    /**
     * 通过策略ID，构建责任链
     *
     * @param strategyId 策略ID
     * @return LogicChain
     */
    public ILogicChain openLogicChain(Long strategyId) {
        StrategyEntity strategy = repository.queryStrategyByStrategyId(strategyId);
        String[] ruleModels = strategy.ruleModels();
        //没有配置规则 就装填默认
        if(ruleModels == null || ruleModels.length == 0) return logicChainMap.get("default");
        ILogicChain logicChain = logicChainMap.get(ruleModels[0]);
        ILogicChain currentChain = logicChain;
        for(int i=1;i<ruleModels.length;i++) {
            ILogicChain chain = logicChainMap.get(ruleModels[i]);
            currentChain = currentChain.appendNext(chain);
        }

        //装填默认责任链
        currentChain.appendNext(logicChainMap.get("default"));
        return logicChain;
    }
}
