package com.project.domain.rebate.service;

import com.project.domain.rebate.model.entity.BehaviorEntity;
import com.project.domain.rebate.model.entity.BehaviorRebateOrderEntity;

import java.util.List;

public interface IBehaviorRebateService {

   List<String> createOrder(BehaviorEntity behaviorEntity);

   BehaviorRebateOrderEntity selectRebateByBizId(String bizId);
}
