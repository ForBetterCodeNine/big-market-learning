package com.project.domain.rebate.model.valobj;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyBehaviorRebateVO {
    private String behaviorType;

    private String rebateDesc;

    private String rebateType;

    private String rebateConfig;

}
