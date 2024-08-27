package com.project.infrastructure.persistent.dao;

import com.project.infrastructure.persistent.po.RaffleActivity;
import com.project.infrastructure.persistent.po.RaffleActivitySku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IRaffleActivityDao {
    RaffleActivity queryRaffleActivityByActivityId(Long activityId);

    Long queryStrategyByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);

    List<RaffleActivitySku> queryActivitySkuListByActivityId(Long activityId);
}
