package com.project.domain.award.service;

import com.project.domain.award.model.entity.DistributeAwardEntity;
import com.project.domain.award.model.entity.UserAwardRecordEntity;

/**
 *  奖品服务接口
 */
public interface IAwardService {
    /**
     * 保存用户中奖信息到表中
     */
    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);

    /**
     * 配送发货奖品
     */
    void distributeAward(DistributeAwardEntity distributeAwardEntity);
}
