package com.project.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则树连线
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleTreeNodeLineVO {
    private String treeId;

    //上一个节点
    private String ruleNodeFrom;

    //下一个节点
    private String ruleNodeTo;

    //限定类型
    private RuleLimitTypeVO ruleLimitType;

    //限定值
    private RuleLogicCheckTypeVO ruleLimitValue;
}
