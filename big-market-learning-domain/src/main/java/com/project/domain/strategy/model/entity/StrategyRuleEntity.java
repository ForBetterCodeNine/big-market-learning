package com.project.domain.strategy.model.entity;

import com.project.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyRuleEntity {

    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖奖品ID【规则类型为策略，则不需要奖品ID】 */
    private Integer awardId;
    /** 抽象规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;
    /** 抽奖规则比值 */
    private String ruleValue;
    /** 抽奖规则描述 */
    private String ruleDesc;

    /**
     * 获取权重值
     * 数据案例；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     */
    public Map<String, List<Integer>> getRuleWeightValues() {
        if(!"rule_weight".equals(ruleModel)) return null;
        //先处理空格
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<String, List<Integer>> result = new HashMap<>();
        for(String valueGroup:ruleValueGroups) {
            //处理冒号
            if(valueGroup == null || valueGroup.isEmpty()) return null;
            String[] parts = valueGroup.split(Constants.COLON);
            if(parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + valueGroup);
            }
            String value = parts[1];
            //继续分割 ，
            List<Integer> list = new ArrayList<>();
            String[] valuePart = value.split(Constants.SPLIT);
            for(String v:valuePart) {
                list.add(Integer.parseInt(v));
            }
            //放入map
            result.put(valueGroup, list);
        }
        return result;
    }
}
