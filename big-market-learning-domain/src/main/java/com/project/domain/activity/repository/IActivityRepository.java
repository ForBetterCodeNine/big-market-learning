package com.project.domain.activity.repository;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.project.domain.activity.model.entity.*;
import com.project.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;
import java.util.List;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    //查询sku信息
    ActivitySkuEntity queryActivitySkuEntity(Long sku);

    //查询活动
    ActivityEntity queryRaffleActivityEntityByAcId(Long activityId);

    //查询活动数量信息
    ActivityCountEntity queryActivityCountEntityByAccountId(Long activityCountId);

    void doSaveNoPayOrder(CreateOrderAggregate createOrderAggregate);

    void doSaveCreditPayOrder(CreateOrderAggregate createOrderAggregate);


    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

    /**
     * 查询用户未使用的订单
     */
    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    /**
     * 查询用户参与额度
     */
    ActivityAccountEntity queryActivityAccountByUserIdAndAcId(String userId, Long activityId);

    /**
     * 查询用户月额度
     */
    ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month);

    /**
     * 查询用户当天额度
     */
    ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day);

    /**
     * 保存订单聚合对象
     */
    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate partakeOrderAggregate);

    List<ActivitySkuEntity> queryActivitySkuListByActivityId(Long activityId);


    Integer queryRaffleActivityDayPartakeCount(Long activityId, String userId);

    void updateOrder(DeliverOrderEntity deliverOrderEntity);
}
