package com.project.domain.strategy.service.rule.tree.factory.engine.impl;

import com.project.domain.strategy.model.valobj.RuleTreeNodeLineVO;
import com.project.domain.strategy.model.valobj.RuleTreeNodeVO;
import com.project.domain.strategy.model.valobj.RuleTreeVO;
import com.project.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.project.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.project.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import com.project.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class DecisionTreeEngine implements IDecisionTreeEngine {


    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    private final RuleTreeVO ruleTreeVO;


    public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeGroup, RuleTreeVO ruleTreeVO) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
        this.ruleTreeVO = ruleTreeVO;
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId) {
        DefaultTreeFactory.StrategyAwardVO strategyAwardVO = null;

        //获取决策树节点
        String treeRootRuleNode = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();
        //获取起始节点
        RuleTreeNodeVO ruleTreeNodeVO = treeNodeMap.get(treeRootRuleNode);
        while(treeRootRuleNode != null) {
            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(ruleTreeNodeVO.getRuleKey());
            //决策节点计算
            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId);
            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = logicEntity.getRuleLogicCheckTypeVO();
            strategyAwardVO = logicEntity.getStrategyAwardData();
            log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeVO.getTreeName(), ruleTreeVO.getTreeId(), treeRootRuleNode, ruleLogicCheckTypeVO.getCode());
            //获取下个节点
            treeRootRuleNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNodeVO.getTreeNodeLineVOList());
            ruleTreeNodeVO = treeNodeMap.get(treeRootRuleNode);
        }

        return strategyAwardVO;
    }

    private String nextNode(String matterValue, List<RuleTreeNodeLineVO> treeNodeLineVOList) {
        if(treeNodeLineVOList == null || treeNodeLineVOList.isEmpty()) return null;
        for(RuleTreeNodeLineVO nodeLine : treeNodeLineVOList) {
            if(decisionLogic(matterValue, nodeLine)) {
                return nodeLine.getRuleNodeTo();
            }
        }
        throw new RuntimeException("决策树引擎，nextNode 计算失败，未找到可执行节点！");
    }

    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLineVO) {
        switch (nodeLineVO.getRuleLimitType()) {
            case EQUAL:
                return matterValue.equals(nodeLineVO.getRuleLimitValue().getCode());
            default:
                return false;
        }

    }
}
