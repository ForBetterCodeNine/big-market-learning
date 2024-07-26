package com.project.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户抽奖结果实体
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RaffleAwardEntity {

    private Long strategyId;

    private Integer awardId;

    private String awardKey;

    private String awardConfig;

    private String awardDesc;
}
