package com.project.trigger.api;

import com.project.trigger.api.dto.ActivityDrawRequestDTO;
import com.project.trigger.api.dto.ActivityDrawResponseDTO;
import com.project.types.model.Response;

/**
 * 抽奖活动服务
 */
public interface IRaffleActivityService {
    /**
     * 活动装配 数据预热缓存
     */
    Response<Boolean> armory(Long activityId);

    /**
     * 活动抽奖接口
     */
    Response<ActivityDrawResponseDTO> draw(ActivityDrawRequestDTO requestDTO);
}
