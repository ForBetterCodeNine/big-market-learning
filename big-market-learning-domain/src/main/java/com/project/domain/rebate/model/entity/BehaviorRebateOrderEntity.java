package com.project.domain.rebate.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateOrderEntity {

    private String userId;

    private String orderId;

    private String behaviorType;

    private String rebateDesc;

    private String rebateConfig;

    private String bizId;
}
