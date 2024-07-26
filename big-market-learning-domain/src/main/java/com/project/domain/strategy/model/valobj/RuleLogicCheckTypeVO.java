package com.project.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleLogicCheckTypeVO {
    ALLOW("0000", "放行，执行后续的流程"),

    TAKE_OVER("0001", "接管，后续流程不再执行")
    ;

    private final String code;

    private final String info;
}
