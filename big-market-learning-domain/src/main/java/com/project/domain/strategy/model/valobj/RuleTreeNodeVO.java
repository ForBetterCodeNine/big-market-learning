package com.project.domain.strategy.model.valobj;

/**
 * 规则树节点
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeNodeVO {
    private String treeId;

    //规则key
    private String ruleKey;

    private String ruleDesc;

    //规则比值
    private String ruleValue;

    //规则连线
    private List<RuleTreeNodeLineVO> treeNodeLineVOList;
}
