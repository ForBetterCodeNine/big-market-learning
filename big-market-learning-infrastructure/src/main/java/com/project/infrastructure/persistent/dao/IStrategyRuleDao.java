package com.project.infrastructure.persistent.dao;


import com.project.infrastructure.persistent.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IStrategyRuleDao {
    List<StrategyRule> queryStrategyRuleList();

    StrategyRule queryStrategyRuleByStrategyIdAndRuleWeight(@Param("strategyId") Long strategyId, @Param("ruleModel") String ruleModel);
}
