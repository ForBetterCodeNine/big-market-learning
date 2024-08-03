package com.project.domain.strategy.model.valobj;

import lombok.*;

/**
 * 用于根据规则id和奖品id查到rule_models
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardRuleModelVO {

    private String ruleModels;

}
