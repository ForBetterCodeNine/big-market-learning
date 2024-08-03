package com.project.domain.strategy.model.valobj;

/**
 * 规则树节点
 */

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class RuleTreeNodeVO {
    private Integer treeId;

    //规则key
    private String ruleKey;

    private String ruleDesc;

    //规则比值
    private String ruleValue;

    //规则连线
    private List<RuleTreeNodeLineVO> treeNodeLineVOList;
}
