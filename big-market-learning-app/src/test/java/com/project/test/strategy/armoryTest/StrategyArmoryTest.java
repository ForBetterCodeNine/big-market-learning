package com.project.test.strategy.armoryTest;

import com.project.domain.strategy.service.armory.IStrategyArmory;
import com.project.domain.strategy.service.armory.IStrategyDispatch;
import com.project.infrastructure.persistent.dao.IStrategyRuleDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class StrategyArmoryTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IStrategyDispatch dispatch;

    @Resource
    private IStrategyRuleDao strategyRuleDao;

    @Before
    public void test_armory() {
        boolean success = strategyArmory.strategyArmory(100001L);
        log.info("测试结果：{}", success);
    }

    @Test
    public  void test_rule_dao() {
        log.info("测试结果：{} - 值", strategyRuleDao.queryStrategyRuleByStrategyIdAndRuleWeight(100001L, "rule_weight"));
    }

    @Test
    public void test_getRandomAward() {
        log.info("测试结果：{} - 奖品ID值", dispatch.getRandomAwardId(100001L));
        log.info("测试结果：{} - 奖品ID值", dispatch.getRandomAwardId(100001L));
        log.info("测试结果：{} - 奖品ID值", dispatch.getRandomAwardId(100001L));
    }

    /**
     * 根据策略ID+权重值，从装配的策略中随机获取奖品ID值
     */
    @Test
    public void test_getRandomAwardId_ruleWeightValue() {
        log.info("测试结果：{} - 4000 策略配置", dispatch.getRandomAwardId(100001L, "4000:102,103,104,105"));
        log.info("测试结果：{} - 5000 策略配置", dispatch.getRandomAwardId(100001L, "5000:102,103,104,105,106,107"));
        log.info("测试结果：{} - 6000 策略配置", dispatch.getRandomAwardId(100001L, "6000:102,103,104,105,106,107,108,109"));
    }

    @Test
    public void test_getRandomAwardId() {
        log.info("测试结果：{} - 奖品ID值", dispatch.getRandomAwardId(100002L));
        log.info("测试结果：{} - 奖品ID值", dispatch.getRandomAwardId(100002L));
        log.info("测试结果：{} - 奖品ID值", dispatch.getRandomAwardId(100002L));

    }
}
