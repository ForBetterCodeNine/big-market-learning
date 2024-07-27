package com.project.domain.strategy.service.raffle;

import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.model.entity.RuleActionEntity;
import com.project.domain.strategy.model.entity.RuleMatterEntity;
import com.project.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.domain.strategy.service.rule.AbstractRaffleStrategy;
import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.project.domain.strategy.service.rule.filter.ILogicFilter;
import com.project.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;


@Service
@Slf4j
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    @Resource
    private DefaultLogicFactory defaultLogicFactory;

    public DefaultRaffleStrategy(DefaultChainFactory defaultChainFactory, IStrategyDispatch strategyDispatch, IStrategyRepository strategyRepository, DefaultLogicFactory defaultLogicFactory) {
        super(defaultChainFactory, strategyDispatch, strategyRepository);
        this.defaultLogicFactory = defaultLogicFactory;
    }


    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity factorEntity, String... logics) {
        if(null == logics || logics.length == 0) {
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }
        Map<String, ILogicFilter<RuleActionEntity.RaffleCenterEntity>> filterMap = defaultLogicFactory.openLogicFilter();
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity = null;
        for(String ruleModel : logics) {
            ILogicFilter<RuleActionEntity.RaffleCenterEntity> logicFilter = filterMap.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity();
            ruleMatterEntity.setUserId(factorEntity.getUserId());
            ruleMatterEntity.setStrategyId(factorEntity.getStrategyId());
            ruleMatterEntity.setAwardId(factorEntity.getAwardId());
            ruleMatterEntity.setRuleModel(ruleModel);
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            log.info("抽奖中规则过滤 userId: {} ruleModel: {} code: {} info: {}", factorEntity.getUserId(), ruleModel, ruleActionEntity.getCode(), ruleActionEntity.getInfo());
            if(!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.getCode())) return ruleActionEntity;
        }
        return ruleActionEntity;
    }
}
