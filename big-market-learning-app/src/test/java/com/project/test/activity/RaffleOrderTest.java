package com.project.test.activity;

import com.project.domain.activity.model.entity.SkuRechargeOrderEntity;
import com.project.domain.activity.service.IRaffleOrder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleOrderTest {
    @Resource
    private IRaffleOrder raffleOrder;

    //@Test
    //public void test_createRaffleActivityOrder() {
    //    ActivityShopCartEntity activityShopCartEntity = new ActivityShopCartEntity();
    //    activityShopCartEntity.setUserId("xiaofuge");
    //    activityShopCartEntity.setSku(9011L);
    //    ActivityOrderEntity raffleActivityOrder = raffleOrder.createRaffleActivityOrder(activityShopCartEntity);
    //    log.info("测试结果：{}", JSON.toJSONString(raffleActivityOrder));
    //}

    @Test
    public void test_createSkuRechargeOrder() {
        SkuRechargeOrderEntity skuRechargeEntity = new SkuRechargeOrderEntity();
        skuRechargeEntity.setUserId("xiaofuge");
        skuRechargeEntity.setSku(9011L);
        // outBusinessNo 作为幂等仿重使用，同一个业务单号2次使用会抛出索引冲突 Duplicate entry '700091009111' for key 'uq_out_business_no' 确保唯一性。
        skuRechargeEntity.setOutBusinessNo("700091009112");
        String orderId = raffleOrder.createSkuRechargeOrder(skuRechargeEntity);
        log.info("测试结果：{}", orderId);
    }
}
