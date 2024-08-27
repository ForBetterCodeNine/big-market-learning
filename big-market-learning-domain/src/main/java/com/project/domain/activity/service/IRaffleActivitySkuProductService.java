package com.project.domain.activity.service;

import com.project.domain.activity.model.entity.SkuProductEntity;

import java.util.List;

public interface IRaffleActivitySkuProductService {
    List<SkuProductEntity> querySkuProductEntityListByActivityId(Long activityId);
}
