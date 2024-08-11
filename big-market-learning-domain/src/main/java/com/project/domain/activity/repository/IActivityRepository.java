package com.project.domain.activity.repository;

import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.entity.ActivityCountEntity;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.ActivitySkuEntity;
import com.project.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;

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

    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);
}
