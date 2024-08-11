package com.project.domain.activity.service;

import com.project.domain.activity.model.entity.PartakeRaffleActivityEntity;
import com.project.domain.activity.model.entity.UserRaffleOrderEntity;

/**
 * 用户参与活动接口 领取活动单 相应库存改变
 */
public interface IRaffleActivityPartakeService {
    /**
     * 创建抽奖单；用户参与抽奖活动，扣减活动账户库存，产生抽奖单。如存在未被使用的抽奖单则直接返回已存在的抽奖单
     */
    UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);
}
