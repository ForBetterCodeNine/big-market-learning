package com.project.domain.activity.service.rule;

public interface IActionArmoryChain {
    IActionChain next();

    IActionChain appendNext(IActionChain next);
}
