package com.project.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyAwardEntity {
    private Long strategyId;

    private Integer awardId;

    private Integer awardCount;

    private Integer awardCountSurplus;

    private BigDecimal awardRate;
}
