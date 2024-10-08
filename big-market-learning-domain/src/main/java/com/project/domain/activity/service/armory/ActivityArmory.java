package com.project.domain.activity.service.armory;

import com.project.domain.activity.model.entity.ActivitySkuEntity;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.types.common.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ActivityArmory implements IActivityArmory, IActivityDispatch {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public boolean assembleActivitySku(Long sku) {
        //预热活动sku库存
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuEntity(sku);
        cacheActivitySkuStockCount(sku, activitySkuEntity.getStockCountSurplus());

        activityRepository.queryRaffleActivityEntityByAcId(activitySkuEntity.getActivityId());
        activityRepository.queryActivityCountEntityByAccountId(activitySkuEntity.getActivityCountId());


        return true;
    }

    @Override
    public boolean assembleActivitySkuByActivityId(Long activityId) {
        List<ActivitySkuEntity> skuEntityList = activityRepository.queryActivitySkuListByActivityId(activityId);
        for(ActivitySkuEntity skuEntity : skuEntityList) {
            cacheActivitySkuStockCount(skuEntity.getSku(), skuEntity.getStockCountSurplus());
            //预热活动次数
            activityRepository.queryActivityCountEntityByAccountId(skuEntity.getActivityCountId());
        }
        activityRepository.queryRaffleActivityEntityByAcId(activityId);
        return false;
    }

    private void cacheActivitySkuStockCount(Long sku, Integer stockCount) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
        activityRepository.cacheActivitySkuStockCount(cacheKey, stockCount);
    }

    @Override
    public boolean subtractionActivitySkuStock(Long sku, Date endDateTime) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
        return activityRepository.subtractionActivitySkuStock(sku, cacheKey, endDateTime);
    }
}
