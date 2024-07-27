package com.project.domain.strategy.model.valobj;

import com.project.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import com.project.types.common.Constants;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于根据规则id和奖品id查到rule_models
 */

@Data
public class StrategyAwardRuleModelVO {

    private String ruleModels;

    public String[] getRuleModels() {
        String[] splitValue = ruleModels.split(Constants.SPLIT);
        List<String> list = new ArrayList<>();
        for(String value : splitValue) {
            if(DefaultLogicFactory.LogicModel.isCenter(value)) {
                list.add(value);
            }
        }
        return list.toArray(new String[0]);
    }

}
