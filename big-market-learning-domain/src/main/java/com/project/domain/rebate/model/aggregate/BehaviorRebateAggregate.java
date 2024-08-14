package com.project.domain.rebate.model.aggregate;

import com.project.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.project.domain.rebate.model.entity.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateAggregate {

    private String userId;

    private BehaviorRebateOrderEntity behaviorRebateOrderEntity;

    private TaskEntity taskEntity;
}
