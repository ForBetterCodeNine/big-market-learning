package com.project.domain.strategy.model.entity;

import lombok.Data;

/**
 * 规则物料实体
 */

@Data
public class RuleMatterEntity {

    private String userId;

    private Long strategyId;

    private Integer awardId;

    private String ruleModel;
}
