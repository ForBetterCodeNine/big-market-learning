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

    /** 奖品配置信息 */
    private String awardConfig;

    private Integer awardId;

    private Integer sort;
}
