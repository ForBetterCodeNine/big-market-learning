package com.project.trigger.api.dto;

import lombok.Data;

/**
 * 抽奖奖品列表 请求对象
 */
@Data
public class RaffleAwardListRequestDTO {
    //抽奖策略id
    private Long strategyId;
}
