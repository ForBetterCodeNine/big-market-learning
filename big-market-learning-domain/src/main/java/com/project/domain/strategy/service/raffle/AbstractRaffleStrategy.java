package com.project.domain.strategy.service.raffle;

import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.model.entity.RuleActionEntity;
import com.project.domain.strategy.model.entity.StrategyEntity;
import com.project.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.project.domain.strategy.model.valobj.StrategyAwardRuleModelVO;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.IRaffleStrategy;
import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.domain.strategy.service.rule.factory.DefaultLogicFactory;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {
    protected IStrategyRepository strategyRepository;

    protected IStrategyDispatch strategyDispatch;

    public AbstractRaffleStrategy(IStrategyRepository strategyRepository, IStrategyDispatch strategyDispatch) {
        this.strategyRepository = strategyRepository;
        this.strategyDispatch = strategyDispatch;
    }

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity factorEntity) {
        String userId = factorEntity.getUserId();
        Long strategyId = factorEntity.getStrategyId();
        if(null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        //查询策略
        StrategyEntity entity = strategyRepository.queryStrategyByStrategyId(strategyId);

        RaffleFactorEntity factor = new RaffleFactorEntity();
        factor.setStrategyId(strategyId);
        factor.setUserId(userId);

        //抽奖前规则过滤
//        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = this.doCheckRaffleBeforeLogic(RaffleFactorEntity.builder().userId(userId)
//                .strategyId(strategyId).build(), entity.getRuleModels());
        RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> ruleActionEntity = this.doCheckRaffleBeforeLogic(factorEntity, entity.ruleModels());

        if(RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionEntity.getCode())) {
            if(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode().equals(ruleActionEntity.getRuleModel())) {
                // 黑名单返回固定的奖品ID
                return RaffleAwardEntity.builder()
                        .awardId(ruleActionEntity.getData().getAwardId())
                        .build();
            }else if(DefaultLogicFactory.LogicModel.RULE_WEIGHT.getCode().equals(ruleActionEntity.getRuleModel())) {
                //权重过滤 则在对应的一些奖品内随机抽一个
                RuleActionEntity.RaffleBeforeEntity raffleBeforeEntity = ruleActionEntity.getData();
                String ruleWeightValueKey = raffleBeforeEntity.getRuleWeightValueKey();
                Integer awardId = strategyDispatch.getRandomAwardId(strategyId, ruleWeightValueKey);
                return RaffleAwardEntity.builder()
                        .awardId(awardId)
                        .build();
            }
        }


        // 4. 默认抽奖流程
        Integer awardId = strategyDispatch.getRandomAwardId(strategyId);

        //根据奖品id和规则id 查表strategy_award 看对应的配置
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = strategyRepository.queryStrategyRuleModelsByStrategyIdAndAwardId(strategyId, awardId);
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

    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity factorEntity, String...logics);

    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity factorEntity, String...logics);
}
