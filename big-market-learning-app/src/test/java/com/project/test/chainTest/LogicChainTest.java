package com.project.test.chainTest;


import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.service.armory.IStrategyArmory;
import com.project.domain.strategy.service.rule.IRaffleStrategy;
import com.project.domain.strategy.service.rule.chain.ILogicChain;
import com.project.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.project.domain.strategy.service.rule.chain.impl.RuleWeightLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LogicChainTest {

    @Resource
    private IStrategyArmory strategyArmory;
    @Resource
    private RuleWeightLogicChain ruleWeightLogicChain;
    @Resource
    private DefaultChainFactory defaultChainFactory;
    @Resource
    private IRaffleStrategy raffleStrategy;

    @Before
    public void setUp() {
        // 策略装配 100001、100002、100003
        log.info("测试结果：{}", strategyArmory.strategyArmory(100001L));
        log.info("测试结果：{}", strategyArmory.strategyArmory(100002L));
        log.info("测试结果：{}", strategyArmory.strategyArmory(100003L));
    }

    @Test
    public void test_LogicChain_rule_blacklist() {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(100001L);
        //Integer awardId = logicChain.doLogic(100001L, "user001");
        DefaultChainFactory.StrategyAwardVO strategyAwardVO = logicChain.doLogic(100001L, "user001");
        log.info("测试结果：{}", strategyAwardVO);
    }

    @Test
    public void test_LogicChain_rule_weight() {
        // 通过反射 mock 规则中的值
        ReflectionTestUtils.setField(ruleWeightLogicChain, "userScore", 4900L);

        ILogicChain logicChain = defaultChainFactory.openLogicChain(100001L);
        DefaultChainFactory.StrategyAwardVO strategyAwardVO = logicChain.doLogic(100001L, "xiaofuge");
        log.info("测试结果：{}", strategyAwardVO);
    }

    @Test
    public void test_LogicChain_rule_default() {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(100001L);
        DefaultChainFactory.StrategyAwardVO strategyAwardVO = logicChain.doLogic(100001L, "xiaofuge");
        log.info("测试结果：{}", strategyAwardVO);
    }

    @Test
    public void test_raffle() {
        RaffleFactorEntity factor = new RaffleFactorEntity();
        factor.setUserId("xiaofuge");
        factor.setStrategyId(100001L);
        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(factor);
        log.info("抽奖结果：{}", raffleAwardEntity.getAwardId());
    }
}
