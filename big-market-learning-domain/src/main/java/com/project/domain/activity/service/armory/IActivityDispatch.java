package com.project.domain.activity.service.armory;

import java.util.Date;

/**
 * 操作库存扣减
 */
public interface IActivityDispatch {
    /**
     * 根据策略id和奖品id 扣减奖品缓存库存
     */
    boolean subtractionActivitySkuStock(Long sku, Date endDateTime);
}
