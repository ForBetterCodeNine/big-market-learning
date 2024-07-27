package com.project.domain.strategy.service.rule;

import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.model.entity.RuleActionEntity;
import com.project.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.project.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.domain.strategy.service.rule.chain.ILogicChain;
import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {
    protected IStrategyRepository strategyRepository;

    protected IStrategyDispatch strategyDispatch;

    private final DefaultChainFactory defaultChainFactory;

    public AbstractRaffleStrategy(DefaultChainFactory defaultChainFactory, IStrategyDispatch strategyDispatch, IStrategyRepository strategyRepository) {
        this.defaultChainFactory = defaultChainFactory;
        this.strategyDispatch = strategyDispatch;
        this.strategyRepository = strategyRepository;
    }

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity factorEntity) {
        String userId = factorEntity.getUserId();
        Long strategyId = factorEntity.getStrategyId();
        if(null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        //获取责任链
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);

        Integer awardId = logicChain.doLogic(strategyId, userId);

        //根据奖品id和规则id 查表strategy_award 看对应的配置
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyRuleModelsByStrategyIdAndAwardId(strategyId, awardId);
        RaffleFactorEntity factor = new RaffleFactorEntity();

        factor.setAwardId(awardId);
        factor.setStrategyId(strategyId);
        factor.setUserId(userId);

        String[] ruleModels = strategyAwardRuleModelVO.getRuleModels();

        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleCenterAction = doCheckRaffleCenterLogic(factorEntity, ruleModels);

        if(RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleCenterAction.getCode())) {
            log.info("【临时日志】中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
            return RaffleAwardEntity.builder()
                    .awardDesc("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
                    .build();
        }

        return RaffleAwardEntity.builder()
                .awardId(awardId)
                .build();
    }

   // protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity factorEntity, String...logics);

    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity factorEntity, String...logics);
}
