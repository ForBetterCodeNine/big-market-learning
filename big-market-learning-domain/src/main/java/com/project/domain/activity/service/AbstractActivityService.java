package com.project.domain.activity.service;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.entity.ActivityCountEntity;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.ActivitySkuEntity;
import com.project.domain.activity.model.entity.SkuRechargeOrderEntity;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.rule.IActionChain;
import com.project.domain.activity.service.rule.factory.DefaultActivityChainFactory;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽奖活动抽象类，定义标准的流程
 */

@Slf4j
public abstract class AbstractActivityService extends RaffleActivitySupport implements IRaffleOrder{

   // protected IActivityRepository activityRepository;

    public AbstractActivityService(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        super(activityRepository, defaultActivityChainFactory);
    }

    @Override
    public String createSkuRechargeOrder(SkuRechargeOrderEntity skuRechargeOrderEntity) {
        String userId = skuRechargeOrderEntity.getUserId();
        Long sku = skuRechargeOrderEntity.getSku();
        String outBusinessNo = skuRechargeOrderEntity.getOutBusinessNo();
        if(sku == null || userId == null || outBusinessNo == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
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

        //保存订单
        doSaveOrder(createOrderAggregate);

        return createOrderAggregate.getActivityOrderEntity().getOrderId();
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
