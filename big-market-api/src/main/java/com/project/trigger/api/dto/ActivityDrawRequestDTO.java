package com.project.trigger.api.dto;

import lombok.Data;

/**
 * 活动抽奖请求对象 进行各个领取的串联
 */

@Data
public class ActivityDrawRequestDTO {

    private String userId;

    private Long activityId;
}
