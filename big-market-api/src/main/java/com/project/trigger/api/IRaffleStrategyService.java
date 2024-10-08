package com.project.trigger.api;

import com.project.trigger.api.dto.RaffleAwardListRequestDTO;
import com.project.trigger.api.dto.RaffleAwardListResponseDTO;
import com.project.trigger.api.dto.RaffleStrategyRequestDTO;
import com.project.trigger.api.dto.RaffleStrategyResponseDTO;
import com.project.types.model.Response;

import java.util.List;

/**
 * 抽奖服务接口
 */
public interface IRaffleStrategyService {

    //策略装配接口
    Response<Boolean> strategyArmory(Long strategyId);

    //查询抽奖奖品列表配置
    Response<List<RaffleAwardListResponseDTO>> queryAwardListResponseDTO(RaffleAwardListRequestDTO requestDTO);

    //随机抽奖接口
    Response<RaffleStrategyResponseDTO> randomRaffle(RaffleStrategyRequestDTO requestDTO);
}
