package com.project.domain.activity.service;

import com.project.domain.activity.model.valobj.ActivitySkuStockKeyVO;

/**
 * 活动sku库存处理接口
 */
public interface ISkuStock {
    /**
     * 获取活动sku库存消耗队列
     */
    ActivitySkuStockKeyVO takeQueueValue() throws Exception;

    /**
     * 清空队列
     */
    void clearQueueValue();

    /**
     * 延迟队列 + 任务趋势更新活动sku库存
     */
    void updateActivitySkuStock(Long sku);

    /**
     * 缓存库存以消耗完毕，清空数据库库存
     */
    void clearActivitySkuStock(Long sku);
}
