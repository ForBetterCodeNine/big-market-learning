package com.project.infrastructure.persistent.dao;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.infrastructure.persistent.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IStrategyAwardDao {
    List<StrategyAward> queryStrategyAwardList();

    List<StrategyAward> queryStrategyAwardEntityListByStrategyId(Long strategyId);
}
