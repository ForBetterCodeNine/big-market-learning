package com.project.domain.strategy.service.rule;

import java.util.Map;

/**
 * 抽奖规则接口；提供对规则的业务功能查询
 */
public interface IRaffleRule {
    /**
     * 根据规则树ID集合查询奖品中加锁数量的配置「部分奖品需要抽奖N次解锁」
     */
    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);
}
