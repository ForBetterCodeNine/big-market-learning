package com.project.test;

import com.alibaba.fastjson2.JSON;
import com.project.domain.strategy.service.rule.IRaffleAward;
import com.project.trigger.api.dto.RaffleAwardListRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Resource
    private IRaffleAward raffleAward;

    @Test
    public void test() {
        RaffleAwardListRequestDTO requestDTO = new RaffleAwardListRequestDTO();
        requestDTO.setUserId("xiaofuge");
        requestDTO.setActivityId(100301L);
        log.info(JSON.toJSONString(requestDTO));
        log.info("测试完成 {}", raffleAward.queryRaffleStrategyAwardListByActivityId(requestDTO.getActivityId()));
    }

}
