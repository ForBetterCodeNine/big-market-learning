package com.project.domain.strategy.model.entity;

import com.project.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StrategyEntity {

    private Long strategyId;

    private String strategyDesc;

    private String ruleModels;

    public String[] ruleModels() {
        if(StringUtils.isBlank(ruleModels)) return null;
        return ruleModels.split(Constants.SPLIT);
    }

    //判断是否含有rule_weight配置
    public String getRuleWeight() {
        String[] ruleModels = ruleModels();
        for(String str:ruleModels) {
            if(str.equals("rule_weight")) return str;
        }
        return null;
    }
}
