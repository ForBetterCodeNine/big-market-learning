package com.project.domain.award.service.distribute;

import com.project.domain.award.model.entity.DistributeAwardEntity;

/**
 * 分发奖品接口
 */
public interface IDistributeAward {
    void giveOutPrizes(DistributeAwardEntity distributeAwardEntity);
}
