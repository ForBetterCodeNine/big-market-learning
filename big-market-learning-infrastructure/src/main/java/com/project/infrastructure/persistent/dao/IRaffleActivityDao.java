package com.project.infrastructure.persistent.dao;

import com.project.infrastructure.persistent.po.RaffleActivity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityDao {
    RaffleActivity queryRaffleActivityByActivityId(Long activityId);

    Long queryStrategyByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);
}
