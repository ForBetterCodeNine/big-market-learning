package com.project.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import com.project.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.project.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.project.domain.rebate.model.entity.TaskEntity;
import com.project.domain.rebate.model.valobj.BehaviorTypeVO;
import com.project.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import com.project.domain.rebate.repository.IBehaviorRebateRepository;
import com.project.infrastructure.event.EventPublisher;
import com.project.infrastructure.persistent.dao.IDailyBehaviorRebateDao;
import com.project.infrastructure.persistent.dao.ITaskDao;
import com.project.infrastructure.persistent.dao.IUserBehaviorRebateOrderDao;
import com.project.infrastructure.persistent.po.DailyBehaviorRebate;
import com.project.infrastructure.persistent.po.Task;
import com.project.infrastructure.persistent.po.UserBehaviorRebateOrder;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class BehaviorRebateRepository implements IBehaviorRebateRepository {

    @Resource
    private IDailyBehaviorRebateDao dailyBehaviorRebateDao;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IUserBehaviorRebateOrderDao userBehaviorRebateOrderDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private EventPublisher eventPublisher;



    @Override
    public List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfigs(BehaviorTypeVO behaviorTypeVO) {
        List<DailyBehaviorRebate> dailyBehaviorRebateList = dailyBehaviorRebateDao.queryDailyBehaviorRebateByBehaviorType(behaviorTypeVO.getCode());
        List<DailyBehaviorRebateVO> resList = new ArrayList<>(dailyBehaviorRebateList.size());
        for(DailyBehaviorRebate rebate : dailyBehaviorRebateList) {
            DailyBehaviorRebateVO vo = new DailyBehaviorRebateVO();
            vo.setBehaviorType(rebate.getBehaviorType());
            vo.setRebateDesc(rebate.getRebateDesc());
            vo.setRebateConfig(rebate.getRebateConfig());
            vo.setRebateType(rebate.getRebateType());
            resList.add(vo);
        }
        return resList;
    }

    @Override
    public void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates) {
        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    for(BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
                        BehaviorRebateOrderEntity behaviorRebateOrderEntity = behaviorRebateAggregate.getBehaviorRebateOrderEntity();
                        TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
                        // 用户行为返利订单对象
                        UserBehaviorRebateOrder userBehaviorRebateOrder = new UserBehaviorRebateOrder();
                        userBehaviorRebateOrder.setUserId(behaviorRebateOrderEntity.getUserId());
                        userBehaviorRebateOrder.setOrderId(behaviorRebateOrderEntity.getOrderId());
                        userBehaviorRebateOrder.setRebateType(behaviorRebateOrderEntity.getBehaviorType());
                        userBehaviorRebateOrder.setRebateConfig(behaviorRebateOrderEntity.getRebateConfig());
                        userBehaviorRebateOrder.setRebateDesc(behaviorRebateOrderEntity.getRebateDesc());
                        userBehaviorRebateOrder.setBehaviorType(behaviorRebateOrderEntity.getBehaviorType());
                        userBehaviorRebateOrder.setBizId(behaviorRebateOrderEntity.getBizId());
                        userBehaviorRebateOrderDao.insert(userBehaviorRebateOrder);

                        Task task = new Task();
                        task.setUserId(taskEntity.getUserId());
                        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
                        task.setMessageId(taskEntity.getMessageId());
                        task.setTopic(taskEntity.getTopic());
                        task.setState(taskEntity.getState().getCode());
                        taskDao.insert(task);

                    }
                    return 1;
                }catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入返利记录，唯一索引冲突 userId: {}", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        }finally {
            dbRouter.clear();
        }

        //同步发送MQ
        for(BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregates) {
            TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
            Task task = new Task();
            task.setUserId(taskEntity.getUserId());
            task.setMessageId(taskEntity.getMessageId());
            try {
                eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                taskDao.updateTaskSendMessageComplete(task);
            }catch (Exception e) {
                log.error("写入返利记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
                taskDao.updateTaskSendMessageFail(task);
            }
        }
    }
}
