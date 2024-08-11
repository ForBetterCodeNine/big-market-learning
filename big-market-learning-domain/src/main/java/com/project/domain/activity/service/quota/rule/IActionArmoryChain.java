package com.project.domain.activity.service.quota.rule;

public interface IActionArmoryChain {
    IActionChain next();

    IActionChain appendNext(IActionChain next);
}
