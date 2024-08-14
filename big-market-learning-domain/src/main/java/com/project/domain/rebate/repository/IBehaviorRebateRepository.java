package com.project.domain.rebate.repository;

import com.project.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.project.domain.rebate.model.valobj.BehaviorTypeVO;
import com.project.domain.rebate.model.valobj.DailyBehaviorRebateVO;

import java.util.List;

public interface IBehaviorRebateRepository {
    List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfigs(BehaviorTypeVO behaviorTypeVO);

    void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);
}
