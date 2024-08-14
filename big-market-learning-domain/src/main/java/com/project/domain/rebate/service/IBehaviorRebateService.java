package com.project.domain.rebate.service;

import com.project.domain.rebate.model.entity.BehaviorEntity;

import java.util.List;

public interface IBehaviorRebateService {

   List<String> createOrder(BehaviorEntity behaviorEntity);
}
