package com.project.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import com.project.domain.award.model.aggregate.GiveoutPrizesAggregate;
import com.project.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.project.domain.award.model.entity.TaskEntity;
import com.project.domain.award.model.entity.UserAwardRecordEntity;
import com.project.domain.award.model.entity.UserCreditAwardEntity;
import com.project.domain.award.model.valobj.AccountStatusVO;
import com.project.domain.award.repository.IAwardRepository;
import com.project.infrastructure.event.EventPublisher;
import com.project.infrastructure.persistent.dao.*;
import com.project.infrastructure.persistent.po.Task;
import com.project.infrastructure.persistent.po.UserAwardRecord;
import com.project.infrastructure.persistent.po.UserCreditAccount;
import com.project.infrastructure.persistent.po.UserRaffleOrder;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Component
public class AwardRepository implements IAwardRepository {

    @Resource
    private IAwardDao awardDao;

    @Resource
    private IUserCreditAccountDao userCreditAccountDao;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Resource
    private IUserAwardRecordDao userAwardRecordDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate) {
        UserAwardRecordEntity userAwardRecordEntity = userAwardRecordAggregate.getUserAwardRecordEntity();
        TaskEntity taskEntity = userAwardRecordAggregate.getTaskEntity();

        String userId = userAwardRecordEntity.getUserId();
        Long activityId = userAwardRecordEntity.getActivityId();
        Integer awardId = userAwardRecordEntity.getAwardId();

        UserAwardRecord userAwardRecord = new UserAwardRecord();
        userAwardRecord.setUserId(userId);
        userAwardRecord.setAwardId(awardId);
        userAwardRecord.setActivityId(activityId);
        userAwardRecord.setOrderId(userAwardRecordEntity.getOrderId());
        userAwardRecord.setAwardTime(userAwardRecordEntity.getAwardTime());
        userAwardRecord.setAwardState(userAwardRecordEntity.getState().getCode());
        userAwardRecord.setAwardTitle(userAwardRecordEntity.getAwardTitle());
        userAwardRecord.setStrategyId(userAwardRecordEntity.getStrategyId());

        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setTopic(taskEntity.getTopic());
        task.setMessageId(taskEntity.getMessageId());
        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
        task.setState(taskEntity.getState().getCode());

        UserRaffleOrder userRaffleOrderReq = new UserRaffleOrder();
        userRaffleOrderReq.setUserId(userId);
        userRaffleOrderReq.setOrderId(userAwardRecordEntity.getOrderId());

        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status ->{
                try {
                    userAwardRecordDao.insert(userAwardRecord);
                    taskDao.insert(task);
                    //更新抽奖单
                    int count = userRaffleOrderDao.updateUserRaffleOrderStateUsed(userRaffleOrderReq);
                    if(count != 1) {
                        status.setRollbackOnly();
                        log.error("写入中奖记录，用户抽奖单已使用过，不可重复抽奖 userId: {} activityId: {} awardId: {}", userId, activityId, awardId);
                        throw new AppException(ResponseCode.ACTIVITY_ORDER_ERROR.getCode(), ResponseCode.ACTIVITY_ORDER_ERROR.getInfo());
                    }
                    return 1;
                }catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入中奖记录，唯一索引冲突 userId: {} activityId: {} awardId: {}", userId, activityId, awardId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), ResponseCode.INDEX_DUP.getInfo());
                }
            });
        }finally {
            dbRouter.clear();
        }

        try {
            //发送消息在任务外执行
            eventPublisher.publish(task.getTopic(), task.getMessage());
            //更新task表
            taskDao.updateTaskSendMessageComplete(task);
        }catch (Exception e) {
            log.error("写入中奖记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskDao.updateTaskSendMessageFail(task);
        }
    }

    @Override
    public String queryAwardConfig(Integer awardId) {
        return awardDao.queryAwardConfigByAwardId(awardId);
    }

    @Override
    public void saveGiveoutPrizesAggregate(GiveoutPrizesAggregate giveoutPrizesAggregate) {
        String userId = giveoutPrizesAggregate.getUserId();
        UserCreditAwardEntity userCreditAwardEntity = giveoutPrizesAggregate.getUserCreditAwardEntity();
        UserAwardRecordEntity userAwardRecordEntity = giveoutPrizesAggregate.getUserAwardRecordEntity();

        //更新发奖记录
        UserAwardRecord userAwardRecordReq = new UserAwardRecord();
        userAwardRecordReq.setUserId(userId);
        userAwardRecordReq.setOrderId(userAwardRecordEntity.getOrderId());
        userAwardRecordReq.setAwardState(userAwardRecordEntity.getState().getCode());

        //更新用户积分
        UserCreditAccount userCreditAccountReq = new UserCreditAccount();
        userCreditAccountReq.setUserId(userId);
        userCreditAccountReq.setTotalAmount(userCreditAwardEntity.getCreditAmount());
        userCreditAccountReq.setAvailableAmount(userCreditAwardEntity.getCreditAmount());
        userCreditAccountReq.setAccountStatus(AccountStatusVO.open.getCode());

        try {
            dbRouter.doRouter(giveoutPrizesAggregate.getUserId());
            transactionTemplate.execute(status -> {
                try {
                    //更新积分 创建积分账户
                    int updateAccountCount = userCreditAccountDao.updateAddAmount(userCreditAccountReq);
                    if(updateAccountCount == 0) {
                        userCreditAccountDao.insert(userCreditAccountReq);
                    }
                    //更新奖品记录
                    int updateAwardCount = userAwardRecordDao.updateAwardRecordCompletedState(userAwardRecordReq);
                    //UserAwardRecord record = userAwardRecordDao.queryAwardRecord(userAwardRecordReq);
                    //if(record == null) {
                    //    log.info("***********************");
                    //}
                    if(updateAwardCount == 0) {
                        log.warn("更新中奖记录，重复更新拦截 userId:{} giveOutPrizesAggregate:{}", userId, JSON.toJSONString(giveoutPrizesAggregate));
                        status.setRollbackOnly();
                    }
                    return 1;
                }catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("更新中奖记录，唯一索引冲突 userId: {} ", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        }finally {
            dbRouter.clear();
        }
    }

    @Override
    public String queryAwardKey(Integer awardId) {
        return awardDao.queryAwardKeyByAwardId(awardId);
    }
}
