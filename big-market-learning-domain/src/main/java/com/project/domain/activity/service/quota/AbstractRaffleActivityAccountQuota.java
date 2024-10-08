package com.project.domain.activity.service.quota;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.entity.*;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.IRaffleActivityAccountQuotaService;
import com.project.domain.activity.service.quota.policy.ITradePolicy;
import com.project.domain.activity.service.quota.rule.IActionChain;
import com.project.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 抽奖活动抽象类，定义标准的流程
 */

@Slf4j
public abstract class AbstractRaffleActivityAccountQuota extends RaffleActivityAccountQuotaSupport implements IRaffleActivityAccountQuotaService {

   // protected IActivityRepository activityRepository;
    private final Map<String, ITradePolicy> tradePolicyMap;


    public AbstractRaffleActivityAccountQuota(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory, Map<String, ITradePolicy> tradePolicyMap) {
        super(activityRepository, defaultActivityChainFactory);
        this.tradePolicyMap = tradePolicyMap;
    }

    @Override
    public UnpaidActivityOrderEntity createSkuRechargeOrder(SkuRechargeOrderEntity skuRechargeOrderEntity) {
        String userId = skuRechargeOrderEntity.getUserId();
        Long sku = skuRechargeOrderEntity.getSku();
        String outBusinessNo = skuRechargeOrderEntity.getOutBusinessNo();
        if(sku == null || userId == null || outBusinessNo == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        //查询未支付订单
        UnpaidActivityOrderEntity unpaidActivityOrderEntity = activityRepository.queryUnpaidActivityOrderEntity(skuRechargeOrderEntity);
        if(unpaidActivityOrderEntity != null) return unpaidActivityOrderEntity;
        //通过sku查询活动信息
        ActivitySkuEntity activitySkuEntity = queryActivitySku(sku);
        //查询活动信息
        ActivityEntity activityEntity = queryActivityEntity(activitySkuEntity.getActivityId());
        //查询次数信息
        ActivityCountEntity activityCountEntity = queryActivityCountEntity(activitySkuEntity.getActivityCountId());

        //活动动作规则校验
        IActionChain chain = defaultActivityChainFactory.openActionChain();
        chain.action(activitySkuEntity, activityEntity, activityCountEntity);

        //构建订单聚合对象
        CreateOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeOrderEntity, activitySkuEntity, activityEntity, activityCountEntity);

        //交易策略 - 【积分兑换，支付类订单】【返利无支付交易订单，直接充值到账】【订单状态变更交易类型策略】
        ITradePolicy tradePolicy = tradePolicyMap.get(skuRechargeOrderEntity.getOrderTradeTypeVO().getCode());
        tradePolicy.trade(createOrderAggregate);
        //保存订单
        //doSaveOrder(createOrderAggregate);
        ActivityOrderEntity activityOrderEntity = createOrderAggregate.getActivityOrderEntity();
        return UnpaidActivityOrderEntity.builder()
                .userId(userId)
                .orderId(activityOrderEntity.getOrderId())
                .outBusinessNo(activityOrderEntity.getOutBusinessNo())
                .payAmount(activityOrderEntity.getPayAmount())
                .build();
    }

    //@Override
    //public ActivityOrderEntity createRaffleActivityOrder(ActivityShopCartEntity activityShopCartEntity) {
    //    //通过sku查询活动信息
    //    ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuEntity(activityShopCartEntity.getSku());
    //    //查询活动信息
    //    ActivityEntity activityEntity = activityRepository.queryRaffleActivityEntityByAcId(activitySkuEntity.getActivityId());
    //    //查询用于可参与次数
    //    ActivityCountEntity activityCountEntity = activityRepository.queryActivityCountEntityByAccountId(activitySkuEntity.getActivityCountId());
    //
    //    log.info("查询结果：{} {} {}", JSON.toJSONString(activitySkuEntity), JSON.toJSONString(activityEntity), JSON.toJSONString(activityCountEntity));
    //
    //    return ActivityOrderEntity.builder().build();
    //}

    protected abstract CreateOrderAggregate buildOrderAggregate(SkuRechargeOrderEntity skuRechargeOrderEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);
}
