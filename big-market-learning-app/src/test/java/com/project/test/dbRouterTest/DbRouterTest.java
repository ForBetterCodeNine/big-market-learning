package com.project.test.dbRouterTest;

import com.alibaba.fastjson2.JSON;
import com.project.infrastructure.persistent.dao.IRaffleActivityDao;
import com.project.infrastructure.persistent.po.RaffleActivity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DbRouterTest {

    @Resource
    private IRaffleActivityDao raffleActivityDao;

    @Test
    public void test_raffle_activity() {
        RaffleActivity raffleActivity = raffleActivityDao.queryRaffleActivityByActivityId(100301L);
        log.info("测试结果：{}", JSON.toJSONString(raffleActivity));
    }
}
