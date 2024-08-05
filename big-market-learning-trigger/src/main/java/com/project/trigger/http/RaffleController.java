package com.project.trigger.http;

import com.alibaba.fastjson.JSON;
import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.service.armory.IStrategyArmory;
import com.project.domain.strategy.service.rule.IRaffleAward;
import com.project.domain.strategy.service.rule.IRaffleStrategy;
import com.project.trigger.api.IRaffleService;
import com.project.trigger.api.dto.RaffleAwardListRequestDTO;
import com.project.trigger.api.dto.RaffleAwardListResponseDTO;
import com.project.trigger.api.dto.RaffleRequestDTO;
import com.project.trigger.api.dto.RaffleResponseDTO;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import com.project.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 营销服务抽奖
 */

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/")
public class RaffleController implements IRaffleService {

    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IRaffleAward raffleAward;

    @Resource
    private IRaffleStrategy raffleStrategy;


    /**
     * 策略装配 将策略信息装配到缓存中
     * @param strategyId
     * @return
     */
    @RequestMapping(value = "strategy_armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> strategyArmory(Long strategyId) {
        try {
            log.info("抽奖策略装配开始 strategyId：{}", strategyId);
            boolean armoryStatus = strategyArmory.strategyArmory(strategyId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(armoryStatus)
                    .build();
            log.info("抽奖策略装配完成 strategyId：{} response: {}", strategyId, JSON.toJSONString(response));
            return response;
        }catch (Exception e) {
            log.error("抽奖策略装配失败 strategyId：{}", strategyId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    /**
     * 查询奖品列表接口
     * @param requestDTO
     * @return
     */
    @RequestMapping(value = "query_raffle_award_list", method = RequestMethod.POST)
    @Override
    public Response<List<RaffleAwardListResponseDTO>> queryAwardListResponseDTO(@RequestBody RaffleAwardListRequestDTO requestDTO) {
        try {
            log.info("查询抽奖奖品列表配开始 strategyId：{}", requestDTO.getStrategyId());
            //查询奖品配置
            List<StrategyAwardEntity> strategyAwardEntityList = raffleAward.queryRaffleStrategyAwardList(requestDTO.getStrategyId());
            List<RaffleAwardListResponseDTO> responseDTOList = new ArrayList<>(strategyAwardEntityList.size());
            for(StrategyAwardEntity strategyAward : strategyAwardEntityList) {
                RaffleAwardListResponseDTO dto = new RaffleAwardListResponseDTO();
                dto.setAwardId(strategyAward.getAwardId());
                dto.setSort(strategyAward.getSort());
                dto.setAwardSubtitle(strategyAward.getAwardSubtitle());
                dto.setAwardTitle(strategyAward.getAwardTitle());
                responseDTOList.add(dto);
            }
            Response<List<RaffleAwardListResponseDTO>> response = Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTOList)
                    .build();
            log.info("查询抽奖奖品列表配置完成 strategyId：{} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        }catch (Exception e) {
            log.error("查询抽奖奖品列表配置失败 strategyId：{}", requestDTO.getStrategyId(), e);
            return Response.<List<RaffleAwardListResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    /**
     * 随机抽奖接口
     * @param requestDTO
     * @return
     */
    @RequestMapping(value = "random_raffle", method = RequestMethod.POST)
    @Override
    public Response<RaffleResponseDTO> randomRaffle(@RequestBody RaffleRequestDTO requestDTO) {
        try {
            log.info("随机抽奖开始 strategyId: {}", requestDTO.getStrategyId());
            RaffleFactorEntity entity = new RaffleFactorEntity();
            entity.setStrategyId(requestDTO.getStrategyId());
            entity.setUserId("zhoujielun");
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(entity);
            Response<RaffleResponseDTO> response = Response.<RaffleResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(RaffleResponseDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();

            log.info("随机抽奖完成 strategyId: {} response: {}", requestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        }catch (AppException e) {
            log.error("随机抽奖失败 strategyId：{} {}", requestDTO.getStrategyId(), e.getInfo());
            return Response.<RaffleResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (Exception e) {
            log.error("随机抽奖失败 strategyId：{}", requestDTO.getStrategyId(), e);
            return Response.<RaffleResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
