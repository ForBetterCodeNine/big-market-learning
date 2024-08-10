package com.project.domain.activity.service.rule.impl;

import com.project.domain.activity.model.entity.ActivityCountEntity;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.ActivitySkuEntity;
import com.project.domain.activity.service.rule.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 商品库存规则节点
 */
@Slf4j
@Component("activity_sku_stock_action")
public class ActivitySkuStockActionChain extends AbstractActionChain {


    @Override
    public boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activity, ActivityCountEntity activityCountEntity) {
        log.info("活动责任链-商品库存处理【校验&扣减】开始。");

        return true;
    }
}
