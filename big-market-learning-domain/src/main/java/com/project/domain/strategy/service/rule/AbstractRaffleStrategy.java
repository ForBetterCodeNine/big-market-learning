package com.project.domain.strategy.service.rule;

import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.project.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {
    protected IStrategyRepository strategyRepository;

    protected IStrategyDispatch strategyDispatch;

    protected final DefaultChainFactory defaultChainFactory;

    protected final DefaultTreeFactory defaultTreeFactory;

    public AbstractRaffleStrategy(DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory, IStrategyDispatch strategyDispatch, IStrategyRepository strategyRepository) {
        this.defaultChainFactory = defaultChainFactory;
        this.defaultTreeFactory = defaultTreeFactory;
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

        DefaultChainFactory.StrategyAwardVO chainStrategyAwardVO = raffleLogicChain(strategyId, userId);
        log.info("抽奖策略计算-责任链 {} {} {} {}", userId, strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getLogicModel());
        //判断是不是走了默认抽奖
        if(!DefaultChainFactory.LogicModel.RULE_DEFAULT.getCode().equals(chainStrategyAwardVO.getLogicModel())) {
            return buildRaffleAwardEntity(strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getAwardRuleValue());
        }

        //走了默认抽奖 则走规则树
        DefaultTreeFactory.StrategyAwardVO treeStrategyAwardVO = raffleLogicTree(userId, strategyId, chainStrategyAwardVO.getAwardId(), factorEntity.getEndDateTime());
        log.info("抽奖策略计算-规则树 {} {} {} {}", userId, strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());

        return buildRaffleAwardEntity(strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());
    }

    private RaffleAwardEntity buildRaffleAwardEntity(Long strategyId, Integer awardId, String config) {
        StrategyAwardEntity strategyAwardEntity = strategyRepository.queryStrategyAwardEntity(strategyId, awardId);
        RaffleAwardEntity awardEntity = new RaffleAwardEntity();
        awardEntity.setAwardId(awardId);
        awardEntity.setSort(strategyAwardEntity.getSort());
        awardEntity.setAwardTitle(strategyAwardEntity.getAwardTitle());
        awardEntity.setAwardConfig(config);
        return awardEntity;
    }

    /**
     * 抽奖计算，责任链抽象方法
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @return 奖品ID
     */
    public abstract DefaultChainFactory.StrategyAwardVO raffleLogicChain(Long strategyId, String userId);


    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId);
    /**
     * 抽奖结果过滤，决策树抽象方法
     *
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return 过滤结果【奖品ID，会根据抽奖次数判断、库存判断、兜底兜里返回最终的可获得奖品信息】
     */
    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId, Date endDateTime);
}
