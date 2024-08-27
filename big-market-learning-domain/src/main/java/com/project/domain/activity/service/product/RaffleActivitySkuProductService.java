package com.project.domain.activity.service.product;

import com.project.domain.activity.model.entity.SkuProductEntity;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.IRaffleActivitySkuProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;


@Service
public class RaffleActivitySkuProductService implements IRaffleActivitySkuProductService {


    @Resource
    private IActivityRepository activityRepository;

    @Override
    public List<SkuProductEntity> querySkuProductEntityListByActivityId(Long activityId) {
        return activityRepository.querySkuProductEntityListByActivityId(activityId);
    }
}
