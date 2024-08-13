package com.project.trigger.http;

import com.project.domain.activity.model.entity.UserRaffleOrderEntity;
import com.project.domain.activity.service.IRaffleActivityPartakeService;
import com.project.domain.activity.service.armory.IActivityArmory;
import com.project.domain.award.model.entity.UserAwardRecordEntity;
import com.project.domain.award.model.valobj.AwardStateVO;
import com.project.domain.award.service.IAwardService;
import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.service.armory.IStrategyArmory;
import com.project.domain.strategy.service.rule.IRaffleStrategy;
import com.project.trigger.api.IRaffleActivityService;
import com.project.trigger.api.dto.ActivityDrawRequestDTO;
import com.project.trigger.api.dto.ActivityDrawResponseDTO;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import com.project.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 抽奖活动服务
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/activity/")
public class RaffleActivityController implements IRaffleActivityService {

    @Resource
    private IActivityArmory activityArmory;

    @Resource
    private IRaffleActivityPartakeService partakeService;

    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IRaffleStrategy raffleStrategy;

    @Resource
    private IAwardService awardService;

    /**
     * 活动装配 数据预热 把活动配置的对应的sku一起装配
     * @param activityId
     * @return
     */
    @RequestMapping(value = "armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> armory(Long activityId) {
        try {
            log.info("活动装配，数据预热，开始 activityId:{}", activityId);
            activityArmory.assembleActivitySkuByActivityId(activityId);
            //策略装配
            strategyArmory.assembleLotteryStrategyByActivity(activityId);

            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
            log.info("活动装配，数据预热，完成 activityId:{}", activityId);
            return response;
        }catch (Exception e) {
            log.error("活动装配，数据预热，失败 activityId:{}", activityId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "draw", method = RequestMethod.POST)
    @Override
    public Response<ActivityDrawResponseDTO> draw(@RequestBody ActivityDrawRequestDTO requestDTO) {
        try {
            String userId = requestDTO.getUserId();
            Long activityId = requestDTO.getActivityId();
            log.info("活动抽奖 userId:{} activityId:{}", userId, activityId);
            if(StringUtils.isBlank(userId) || activityId == null) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            //参与活动
            UserRaffleOrderEntity orderEntity = partakeService.createOrder(userId, activityId);
            log.info("活动抽奖，创建订单 userId:{} activityId:{} orderId:{}", requestDTO.getUserId(), requestDTO.getActivityId(), orderEntity.getOrderId());
            //抽奖策略执行抽奖
            RaffleAwardEntity awardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    .userId(userId)
                    .strategyId(orderEntity.getStrategyId())
                    .endDateTime(orderEntity.getEndDateTime())
                    .build());

            //存放结果 保存到UserAwardRecord表中
            UserAwardRecordEntity userAwardRecordEntity = UserAwardRecordEntity.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .strategyId(orderEntity.getStrategyId())
                    .orderId(orderEntity.getOrderId())
                    .state(AwardStateVO.create)
                    .awardId(awardEntity.getAwardId())
                    .awardTitle(awardEntity.getAwardTitle())
                    .awardTime(new Date())
                    .build();
            awardService.saveUserAwardRecord(userAwardRecordEntity);

            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ActivityDrawResponseDTO.builder()
                            .awardId(awardEntity.getAwardId())
                            .awardTitle(awardEntity.getAwardTitle())
                            .awardIndex(awardEntity.getSort())
                            .build())
                    .build();
        }catch (AppException e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", requestDTO.getUserId(), requestDTO.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (Exception e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", requestDTO.getUserId(), requestDTO.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
