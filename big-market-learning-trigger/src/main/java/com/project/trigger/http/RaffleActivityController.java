package com.project.trigger.http;

import com.alibaba.fastjson.JSON;
import com.project.domain.activity.model.entity.SkuProductEntity;
import com.project.domain.activity.model.entity.SkuRechargeOrderEntity;
import com.project.domain.activity.model.entity.UnpaidActivityOrderEntity;
import com.project.domain.activity.model.entity.UserRaffleOrderEntity;
import com.project.domain.activity.model.valobj.OrderTradeTypeVO;
import com.project.domain.activity.service.IRaffleActivityPartakeService;
import com.project.domain.activity.service.IRaffleActivitySkuProductService;
import com.project.domain.activity.service.armory.IActivityArmory;
import com.project.domain.activity.service.quota.RaffleActivityAccountQuotaService;
import com.project.domain.award.model.entity.UserAwardRecordEntity;
import com.project.domain.award.model.valobj.AwardStateVO;
import com.project.domain.award.service.IAwardService;
import com.project.domain.credit.model.entity.CreditAccountEntity;
import com.project.domain.credit.model.entity.TradeEntity;
import com.project.domain.credit.model.valobj.TradeNameVO;
import com.project.domain.credit.model.valobj.TradeTypeVO;
import com.project.domain.credit.service.ICreditAdjustService;
import com.project.domain.rebate.model.entity.BehaviorEntity;
import com.project.domain.rebate.model.valobj.BehaviorTypeVO;
import com.project.domain.rebate.service.IBehaviorRebateService;
import com.project.domain.strategy.model.entity.RaffleAwardEntity;
import com.project.domain.strategy.model.entity.RaffleFactorEntity;
import com.project.domain.strategy.service.armory.IStrategyArmory;
import com.project.domain.strategy.service.rule.IRaffleStrategy;
import com.project.trigger.api.IRaffleActivityService;
import com.project.trigger.api.dto.ActivityDrawRequestDTO;
import com.project.trigger.api.dto.ActivityDrawResponseDTO;
import com.project.trigger.api.dto.SkuProductResponseDTO;
import com.project.trigger.api.dto.SkuProductShopCartRequestDTO;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import com.project.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 抽奖活动服务
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/activity/")
public class RaffleActivityController implements IRaffleActivityService {
    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyyMMdd");

    @Resource
    private IRaffleActivitySkuProductService raffleActivitySkuProductService;

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


    @Resource
    private IBehaviorRebateService behaviorRebateService;

    @Resource
    private ICreditAdjustService creditAdjustService;
    @Autowired
    private RaffleActivityAccountQuotaService raffleActivityAccountQuotaService;

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
                    .awardConfig(awardEntity.getAwardConfig())
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

    @RequestMapping(value = "calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> calendarSignRebate(@RequestParam String userId) {
        try {
            log.info("日历签到返利开始 userId:{}", userId);
            BehaviorEntity behaviorEntity = new BehaviorEntity();
            behaviorEntity.setUserId(userId);
            behaviorEntity.setBehaviorTypeVO(BehaviorTypeVO.SIGN);
            behaviorEntity.setOutBusinessNo(dateFormatDay.format(new Date()));
            List<String> orderIds = behaviorRebateService.createOrder(behaviorEntity);
            log.info("日历签到返利完成 userId:{} orderIds: {}", userId, JSON.toJSONString(orderIds));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        }catch (AppException e) {
            log.error("日历签到返利异常 userId:{} ", userId, e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (Exception e) {
            log.error("日历签到返利失败 userId:{}", userId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * 查询sku商品集合信息
     * @param activityId
     * @return
     */
    @Override
    public Response<List<SkuProductResponseDTO>> querySkuProductListByActivityId(Long activityId) {
        try {
            log.info("查询sku商品集合开始 activityId:{}", activityId);
            if(activityId == null) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            List<SkuProductEntity> skuProductEntityList = raffleActivitySkuProductService.querySkuProductEntityListByActivityId(activityId);
            List<SkuProductResponseDTO> skuProductResponseDTOS = new ArrayList<>();
            for(SkuProductEntity skuProductEntity : skuProductEntityList) {
                SkuProductResponseDTO.ActivityCount activityCount = new SkuProductResponseDTO.ActivityCount();
                activityCount.setDayCount(skuProductEntity.getActivityCount().getDayCount());
                activityCount.setTotalCount(skuProductEntity.getActivityCount().getTotalCount());
                activityCount.setMonthCount(skuProductEntity.getActivityCount().getMonthCount());

                SkuProductResponseDTO skuProductResponseDTO = new SkuProductResponseDTO();
                skuProductResponseDTO.setSku(skuProductEntity.getSku());
                skuProductResponseDTO.setActivityId(skuProductEntity.getActivityId());
                skuProductResponseDTO.setActivityCountId(skuProductEntity.getActivityCountId());
                skuProductResponseDTO.setStockCount(skuProductEntity.getStockCount());
                skuProductResponseDTO.setStockCountSurplus(skuProductEntity.getStockCountSurplus());
                skuProductResponseDTO.setProductAmount(skuProductEntity.getProductAmount());
                skuProductResponseDTO.setActivityCount(activityCount);

                skuProductResponseDTOS.add(skuProductResponseDTO);
            }

            log.info("查询sku商品集合完成 activityId:{} skuProductResponseDTOS:{}", activityId, JSON.toJSONString(skuProductResponseDTOS));

            return Response.<List<SkuProductResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(skuProductResponseDTOS)
                    .build();
        }catch (Exception e) {
            log.error("查询sku商品集合失败 activityId:{}", activityId, e);
            return Response.<List<SkuProductResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        }
    }


    @Override
    public Response<BigDecimal> queryUserCreditAccount(String userId) {
        try {
            log.info("查询用户积分值开始 userId:{}", userId);
            CreditAccountEntity creditAccountEntity = creditAdjustService.queryUserCreditAccount(userId);
            log.info("查询用户积分值完成 userId:{} adjustAmount:{}", userId, creditAccountEntity.getAdjustAmount());

            return Response.<BigDecimal>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(creditAccountEntity.getAdjustAmount())
                    .build();
        }catch (Exception e) {
            log.error("查询用户积分值失败 userId:{}", userId, e);
            return Response.<BigDecimal>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    /**
     * 积分兑换商品
     * @param requestDTO
     * @return
     */
    @RequestMapping(value = "credit_pay_exchange_sku", method = RequestMethod.POST)
    @Override
    public Response<Boolean> creditPayExchangeSku(SkuProductShopCartRequestDTO requestDTO) {
        try {
            log.info("积分兑换商品开始 userId:{} sku:{}", requestDTO.getUserId(), requestDTO.getSku());
            UnpaidActivityOrderEntity unpaidActivityOrderEntity = raffleActivityAccountQuotaService.createSkuRechargeOrder(
                    SkuRechargeOrderEntity.builder()
                            .userId(requestDTO.getUserId())
                            .sku(requestDTO.getSku())
                            .outBusinessNo(RandomStringUtils.randomNumeric(12))
                            .orderTradeTypeVO(OrderTradeTypeVO.credit_pay_trade)
                            .build()
            );

            log.info("积分兑换商品，创建订单完成 userId:{} sku:{} outBusinessNo:{}", requestDTO.getUserId(), requestDTO.getSku(), unpaidActivityOrderEntity.getOutBusinessNo());
            String orderId = creditAdjustService.createOrder(TradeEntity.builder()
                    .userId(requestDTO.getUserId())
                    .tradeName(TradeNameVO.CONVERT_SKU)
                    .tradeType(TradeTypeVO.REVERSE)
                    .amount(unpaidActivityOrderEntity.getPayAmount().negate())
                    .outBusinessNo(unpaidActivityOrderEntity.getOutBusinessNo())
                    .build());
            log.info("积分兑换商品，支付订单完成  userId:{} sku:{} orderId:{}", requestDTO.getUserId(), requestDTO.getSku(), orderId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        }catch (Exception e) {
            log.error("积分兑换商品失败 userId:{} sku:{}", requestDTO.getUserId(), requestDTO.getSku(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }
}
