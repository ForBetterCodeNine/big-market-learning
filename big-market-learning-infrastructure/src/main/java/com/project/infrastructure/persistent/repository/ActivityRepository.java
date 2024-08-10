package com.project.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.entity.ActivityCountEntity;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.ActivityOrderEntity;
import com.project.domain.activity.model.entity.ActivitySkuEntity;
import com.project.domain.activity.model.valobj.ActivityStateVO;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.infrastructure.persistent.dao.*;
import com.project.infrastructure.persistent.po.*;
import com.project.infrastructure.persistent.redis.IRedisService;
import com.project.types.common.Constants;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IRedisService redisService;

    @Resource
    private IRaffleActivityDao raffleActivityDao;

    @Resource
    private IRaffleActivitySkuDao raffleActivitySkuDao;

    @Resource
    private IRaffleActivityCountDao raffleActivityCountDao;

    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao;

    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouter;


    @Override
    public ActivitySkuEntity queryActivitySkuEntity(Long sku) {
        RaffleActivitySku raffleActivitySku = raffleActivitySkuDao.queryActivitySku(sku);
        return ActivitySkuEntity.builder()
                .activityCountId(raffleActivitySku.getActivityCountId())
                .sku(raffleActivitySku.getSku())
                .activityId(raffleActivitySku.getActivityId())
                .stockCount(raffleActivitySku.getStockCount())
                .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                .build();
    }

    @Override
    public ActivityEntity queryRaffleActivityEntityByAcId(Long activityId) {
        String cacheKey = Constants.RedisKey.ACTIVITY_KEY + activityId;
        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        if(activityEntity != null) return activityEntity;
        RaffleActivity raffleActivity = raffleActivityDao.queryRaffleActivityByActivityId(activityId);
        activityEntity = ActivityEntity.builder()
                .activityId(raffleActivity.getActivityId())
                .activityName(raffleActivity.getActivityName())
                .activityDesc(raffleActivity.getActivityDesc())
                .beginDateTime(raffleActivity.getBeginDateTime())
                .endDateTime(raffleActivity.getEndDateTime())
                .strategyId(raffleActivity.getStrategyId())
                .state(ActivityStateVO.valueOf(raffleActivity.getState()))
                .build();
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityCountEntity queryActivityCountEntityByAccountId(Long activityCountId) {
        String cacheKey = Constants.RedisKey.ACTIVITY_COUNT_KEY + activityCountId;
        ActivityCountEntity activityCountEntity = redisService.getValue(cacheKey);
        if(activityCountEntity != null) return activityCountEntity;
        RaffleActivityCount raffleActivityCount = raffleActivityCountDao.queryRaffleActivityCountByActivityCountId(activityCountId);
        activityCountEntity = ActivityCountEntity.builder()
                .activityCountId(raffleActivityCount.getActivityCountId())
                .totalCount(raffleActivityCount.getTotalCount())
                .dayCount(raffleActivityCount.getDayCount())
                .monthCount(raffleActivityCount.getMonthCount())
                .build();
        redisService.setValue(cacheKey, activityCountEntity);
        return activityCountEntity;
    }

    @Override
    public void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
        try {
            // 订单对象
            ActivityOrderEntity activityOrderEntity = createOrderAggregate.getActivityOrderEntity();
            RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
            raffleActivityOrder.setUserId(activityOrderEntity.getUserId());
            raffleActivityOrder.setSku(activityOrderEntity.getSku());
            raffleActivityOrder.setActivityId(activityOrderEntity.getActivityId());
            raffleActivityOrder.setActivityName(activityOrderEntity.getActivityName());
            raffleActivityOrder.setStrategyId(activityOrderEntity.getStrategyId());
            raffleActivityOrder.setOrderId(activityOrderEntity.getOrderId());
            raffleActivityOrder.setOrderTime(activityOrderEntity.getOrderTime());
            raffleActivityOrder.setTotalCount(activityOrderEntity.getTotalCount());
            raffleActivityOrder.setDayCount(activityOrderEntity.getDayCount());
            raffleActivityOrder.setMonthCount(activityOrderEntity.getMonthCount());
            raffleActivityOrder.setTotalCount(createOrderAggregate.getTotalCount());
            raffleActivityOrder.setDayCount(createOrderAggregate.getDayCount());
            raffleActivityOrder.setMonthCount(createOrderAggregate.getMonthCount());
            raffleActivityOrder.setState(activityOrderEntity.getState().getCode());
            raffleActivityOrder.setOutBusinessNo(activityOrderEntity.getOutBusinessNo());

            // 账户对象
            RaffleActivityAccount raffleActivityAccount = new RaffleActivityAccount();
            raffleActivityAccount.setUserId(createOrderAggregate.getUserId());
            raffleActivityAccount.setActivityId(createOrderAggregate.getActivityId());
            raffleActivityAccount.setTotalCount(createOrderAggregate.getTotalCount());
            raffleActivityAccount.setTotalCountSurplus(createOrderAggregate.getTotalCount());
            raffleActivityAccount.setDayCount(createOrderAggregate.getDayCount());
            raffleActivityAccount.setDayCountSurplus(createOrderAggregate.getDayCount());
            raffleActivityAccount.setMonthCount(createOrderAggregate.getMonthCount());
            raffleActivityAccount.setMonthCountSurplus(createOrderAggregate.getMonthCount());

            dbRouter.doRouter(createOrderAggregate.getUserId());

            //编程式事务
            transactionTemplate.execute(status -> {
                try {
                    //写入订单
                    raffleActivityOrderDao.insert(raffleActivityOrder);
                    //更新账户
                    int count = raffleActivityAccountDao.updateAccountQuota(raffleActivityAccount);
                    if(count == 0) {
                        raffleActivityAccountDao.insert(raffleActivityAccount);
                    }
                    return 1;
                }catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入订单记录，唯一索引冲突 userId: {} activityId: {} sku: {}", activityOrderEntity.getUserId(), activityOrderEntity.getActivityId(), activityOrderEntity.getSku(), e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode());
                }
            });
        }finally {
            dbRouter.clear();
        }
    }
}
