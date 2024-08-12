package com.project.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import com.project.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.project.domain.award.model.entity.TaskEntity;
import com.project.domain.award.model.entity.UserAwardRecordEntity;
import com.project.domain.award.repository.IAwardRepository;
import com.project.infrastructure.event.EventPublisher;
import com.project.infrastructure.persistent.dao.ITaskDao;
import com.project.infrastructure.persistent.dao.IUserAwardRecordDao;
import com.project.infrastructure.persistent.po.Task;
import com.project.infrastructure.persistent.po.UserAwardRecord;
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
    private IDBRouterStrategy dbRouter;

    @Resource
    private IUserAwardRecordDao userAwardRecordDao;

    @Resource
    private ITaskDao taskDao;

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

        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status ->{
                try {
                    userAwardRecordDao.insert(userAwardRecord);
                    taskDao.insert(task);
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
}
