package com.project.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 规则树  树根
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeVO {
    private String treeId;

    //规则树名称
    private String treeName;

    private String treeDesc;

    //规则根节点
    private String treeRootRuleNode;

    //规则节点集合
    private Map<String, RuleTreeNodeVO> treeNodeMap;



}
