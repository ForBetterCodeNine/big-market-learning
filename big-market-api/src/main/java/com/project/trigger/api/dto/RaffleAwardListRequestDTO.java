package com.project.trigger.api.dto;

import lombok.Data;

/**
 * 抽奖奖品列表 请求对象
 */
@Data
public class RaffleAwardListRequestDTO {

    private String userId;


    private Long activityId;


}
