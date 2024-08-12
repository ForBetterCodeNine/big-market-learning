package com.project.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.project.domain.activity.event.ActivitySkuStockZeroMessageEvent;
import com.project.domain.activity.model.aggregate.CreateOrderAggregate;
import com.project.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.project.domain.activity.model.entity.*;
import com.project.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import com.project.domain.activity.model.valobj.ActivityStateVO;
import com.project.domain.activity.model.valobj.UserRaffleOrderStateVO;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.infrastructure.event.EventPublisher;
import com.project.infrastructure.persistent.dao.*;
import com.project.infrastructure.persistent.po.*;
import com.project.infrastructure.persistent.redis.IRedisService;
import com.project.types.common.Constants;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private ActivitySkuStockZeroMessageEvent activitySkuStockZeroMessageEvent;

    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;

    @Resource
    private IRaffleActivityAccountMonthDao raffleActivityAccountMonthDao;

    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;


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

    @Override
    public void cacheActivitySkuStockCount(String cacheKey, Integer stockCount) {
        if(redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, stockCount);
    }

    @Override
    public boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);
        if(surplus == 0) {
            // 库存消耗没了以后，发送MQ消息，更新数据库库存
            eventPublisher.publish(activitySkuStockZeroMessageEvent.topic(), activitySkuStockZeroMessageEvent.buildEventMessage(sku));
            return false;
        }else if(surplus < 0) {
            //恢复为0
            redisService.setAtomicLong(cacheKey, 0);
            return false;
        }

        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等【运营是人来操作，会有这种情况发放，系统要做防护】，也不会超卖。因为所有的可用库存key，都被加锁了。
        // 3. 设置加锁时间为活动到期 + 延迟1天
        String lockKey = cacheKey + "_" + surplus;
        long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        Boolean lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        if(!lock) {
            log.info("活动sku库存加锁失败 {}", lockKey);
        }
        return lock;
    }

    @Override
    public void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        RBlockingQueue<ActivitySkuStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<ActivitySkuStockKeyVO> delayQueue = redisService.getDelayedQueue(blockingQueue);
        delayQueue.offer(activitySkuStockKeyVO, 3, TimeUnit.SECONDS);
    }

    @Override
    public ActivitySkuStockKeyVO takeQueueValue() {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        RBlockingQueue<ActivitySkuStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        return destinationQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_COUNT_QUERY_KEY;
        RBlockingQueue<ActivitySkuStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        destinationQueue.clear();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        raffleActivitySkuDao.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        raffleActivitySkuDao.clearActivitySkuStock(sku);
    }

    @Override
    public UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        UserRaffleOrder order = new UserRaffleOrder();
        order.setActivityId(partakeRaffleActivityEntity.getActivityId());
        order.setUserId(partakeRaffleActivityEntity.getUserId());
        UserRaffleOrder userRaffleOrder = userRaffleOrderDao.queryNoUsedRaffleOrder(order);
        if(userRaffleOrder == null) return null;
        UserRaffleOrderEntity userRaffleOrderEntity = new UserRaffleOrderEntity();
        userRaffleOrderEntity.setUserId(userRaffleOrder.getUserId());
        userRaffleOrderEntity.setActivityId(userRaffleOrder.getActivityId());
        userRaffleOrderEntity.setActivityName(userRaffleOrder.getActivityName());
        userRaffleOrderEntity.setStrategyId(userRaffleOrder.getStrategyId());
        userRaffleOrderEntity.setOrderId(userRaffleOrder.getOrderId());
        userRaffleOrderEntity.setOrderTime(userRaffleOrder.getOrderTime());
        userRaffleOrderEntity.setOrderState(UserRaffleOrderStateVO.valueOf(userRaffleOrder.getOrderState()));
        return userRaffleOrderEntity;
    }

    @Override
    public ActivityAccountEntity queryActivityAccountByUserIdAndAcId(String userId, Long activityId) {
        RaffleActivityAccount activityAccount = new RaffleActivityAccount();
        activityAccount.setActivityId(activityId);
        activityAccount.setUserId(userId);
        RaffleActivityAccount activityAccountRes = raffleActivityAccountDao.queryActivityAccountByUserIdAndAcId(activityAccount);
        if(activityAccountRes == null) return null;
        ActivityAccountEntity activityAccountEntity = new ActivityAccountEntity();
        activityAccountEntity.setActivityId(activityId);
        activityAccountEntity.setUserId(userId);
        activityAccountEntity.setMonthCount(activityAccountRes.getMonthCount());
        activityAccountEntity.setTotalCount(activityAccountRes.getTotalCount());
        activityAccountEntity.setTotalCountSurplus(activityAccountRes.getTotalCountSurplus());
        activityAccountEntity.setDayCount(activityAccountRes.getDayCount());
        activityAccountEntity.setDayCountSurplus(activityAccountRes.getDayCountSurplus());
        activityAccountEntity.setMonthCountSurplus(activityAccountRes.getMonthCountSurplus());
        return activityAccountEntity;
    }

    @Override
    public ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month) {
        RaffleActivityAccountMonth activityAccountMonth = new RaffleActivityAccountMonth();
        activityAccountMonth.setActivityId(activityId);
        activityAccountMonth.setUserId(userId);
        activityAccountMonth.setMonth(month);
        RaffleActivityAccountMonth res = raffleActivityAccountMonthDao.queryActivityAccountMonth(activityAccountMonth);
        if(res == null) return null;
        ActivityAccountMonthEntity entity = new ActivityAccountMonthEntity();
        entity.setActivityId(activityId);
        entity.setUserId(userId);
        entity.setMonth(month);
        entity.setMonthCount(res.getMonthCount());
        entity.setMonthCountSurplus(res.getMonthCountSurplus());
        return entity;
    }

    @Override
    public ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day) {
        RaffleActivityAccountDay activityAccountDay = new RaffleActivityAccountDay();
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setUserId(userId);
        activityAccountDay.setDay(day);
        RaffleActivityAccountDay res = raffleActivityAccountDayDao.queryActivityAccountDay(activityAccountDay);
        if(res == null) return null;
        ActivityAccountDayEntity entity = new ActivityAccountDayEntity();
        entity.setActivityId(activityId);
        entity.setUserId(userId);
        entity.setDayCountSurplus(res.getDayCountSurplus());
        entity.setDay(day);
        entity.setDayCount(res.getDayCount());
        return entity;
    }

    @Override
    public void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate partakeOrderAggregate) {
        try{
            String userId = partakeOrderAggregate.getUserId();
            Long activityId = partakeOrderAggregate.getActivityId();
            ActivityAccountDayEntity activityAccountDayEntity = partakeOrderAggregate.getActivityAccountDayEntity();
            ActivityAccountMonthEntity activityAccountMonthEntity = partakeOrderAggregate.getActivityAccountMonthEntity();
            UserRaffleOrderEntity userRaffleOrderEntity = partakeOrderAggregate.getUserRaffleOrderEntity();
            ActivityAccountEntity activityAccountEntity = partakeOrderAggregate.getActivityAccountEntity();

            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    //更新用户总账户
                    int totalCnt = raffleActivityAccountDao.updateActivityAccountSubtractionQuota(RaffleActivityAccount.builder()
                            .userId(userId)
                            .activityId(activityId)
                            .build());
                    if(1 != totalCnt) {
                        status.setRollbackOnly();
                        log.warn("写入创建参与活动记录，更新总账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
                        throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
                    }

                    //创建或者更新月账户 存在就更新 不存在就插入 因为有总额度
                    if(partakeOrderAggregate.isExistAccountMonth()) {
                        int updateMonthCnt = raffleActivityAccountMonthDao.updateActivityAccountMonthSubstractionQuota(RaffleActivityAccountMonth.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .month(activityAccountMonthEntity.getMonth())
                                .build());

                        if(1 != updateMonthCnt) {
                            status.setRollbackOnly();
                            log.warn("写入创建参与活动记录，更新月账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
                            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
                        }
                        // 更新总账户中月镜像库存
                        raffleActivityAccountDao.updateActivityAccountMonthSubtractionQuota(
                                RaffleActivityAccount.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .build());
                    }else {
                        raffleActivityAccountMonthDao.insertActivityAccountMonth(RaffleActivityAccountMonth.builder()
                                .userId(userId)
                                .month(activityAccountMonthEntity.getMonth())
                                .activityId(activityId)
                                .monthCount(activityAccountMonthEntity.getMonthCount())
                                .monthCountSurplus(activityAccountMonthEntity.getMonthCountSurplus() - 1)
                                .build());
                        // 新创建月账户，则更新总账表中月镜像额度
                        raffleActivityAccountDao.updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .monthCountSurplus(activityAccountEntity.getMonthCountSurplus())
                                .build());
                    }

                    //创建或者更新日账号 存在就更新 不存在就插入
                    if(partakeOrderAggregate.isExistAccountDay()) {
                        int updateDayCnt = raffleActivityAccountDayDao.updateActivityAccountDaySubstractionQuota(RaffleActivityAccountDay.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .day(activityAccountDayEntity.getDay())
                                .build());
                        if(updateDayCnt != 1) {
                            status.setRollbackOnly();
                            log.warn("写入创建参与活动记录，更新日账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
                            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
                        }
                        // 更新总账户中日镜像库存
                        raffleActivityAccountDao.updateActivityAccountDaySubtractionQuota(
                                RaffleActivityAccount.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .build());
                    }else {
                        raffleActivityAccountDayDao.insertActivityAccountDay(RaffleActivityAccountDay.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .day(activityAccountDayEntity.getDay())
                                .dayCount(activityAccountDayEntity.getDayCount())
                                .dayCountSurplus(activityAccountDayEntity.getDayCountSurplus() - 1)
                                .build());

                        //新创建日庄户 更新总账户中日镜像额度
                        raffleActivityAccountDao.updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                                .build());
                    }

                    //写入活动订单
                    userRaffleOrderDao.insert(UserRaffleOrder.builder()
                            .userId(userId)
                            .activityId(activityId)
                            .orderId(userRaffleOrderEntity.getOrderId())
                            .strategyId(userRaffleOrderEntity.getStrategyId())
                            .orderTime(userRaffleOrderEntity.getOrderTime())
                            .activityName(userRaffleOrderEntity.getActivityName())
                            .orderState(userRaffleOrderEntity.getOrderState().getCode())
                            .build());

                    return 1;
                }catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入创建参与活动记录，唯一索引冲突 userId: {} activityId: {}", userId, activityId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        }finally {
            dbRouter.clear();
        }
    }
}
