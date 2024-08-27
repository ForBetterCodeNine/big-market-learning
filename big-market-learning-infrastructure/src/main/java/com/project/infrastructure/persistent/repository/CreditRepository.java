package com.project.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.alibaba.fastjson.JSON;
import com.project.domain.credit.model.aggregate.TradeAggregate;
import com.project.domain.credit.model.entity.CreditAccountEntity;
import com.project.domain.credit.model.entity.CreditOrderEntity;
import com.project.domain.credit.model.entity.TaskEntity;
import com.project.domain.credit.repository.ICreditRepository;
import com.project.infrastructure.event.EventPublisher;
import com.project.infrastructure.persistent.dao.ITaskDao;
import com.project.infrastructure.persistent.dao.IUserCreditAccountDao;
import com.project.infrastructure.persistent.dao.IUserCreditOrderDao;
import com.project.infrastructure.persistent.po.Task;
import com.project.infrastructure.persistent.po.UserCreditAccount;
import com.project.infrastructure.persistent.po.UserCreditOrder;
import com.project.infrastructure.persistent.redis.IRedisService;
import com.project.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Slf4j
@Repository
public class CreditRepository implements ICreditRepository {

    @Resource
    private IRedisService redisService;

    @Resource
    private IUserCreditAccountDao userCreditAccountDao;

    @Resource
    private IUserCreditOrderDao userCreditOrderDao;

    @Resource
    private IDBRouterStrategy idbRouterStrategy;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private EventPublisher eventPublisher;


    @Override
    public void saveUserCreditTradeOrder(TradeAggregate tradeAggregate) {
        String userId = tradeAggregate.getUserId();
        CreditAccountEntity creditAccountEntity = tradeAggregate.getCreditAccountEntity();
        CreditOrderEntity creditOrderEntity = tradeAggregate.getCreditOrderEntity();
        TaskEntity taskEntity = tradeAggregate.getTaskEntity();

        //积分账户
        UserCreditAccount userCreditAccount = new UserCreditAccount();
        userCreditAccount.setUserId(userId);
        userCreditAccount.setTotalAmount(creditAccountEntity.getAdjustAmount());
        userCreditAccount.setAvailableAmount(creditAccountEntity.getAdjustAmount());

        //积分订单
        UserCreditOrder userCreditOrder = new UserCreditOrder();
        userCreditOrder.setUserId(userId);
        userCreditOrder.setOrderId(creditOrderEntity.getOrderId());
        userCreditOrder.setTradeName(creditOrderEntity.getTradeName().getName());
        userCreditOrder.setTradeType(creditOrderEntity.getTradeType().getCode());
        userCreditOrder.setTradeAmount(creditOrderEntity.getTradeAmount());
        userCreditOrder.setOutBusinessNo(creditOrderEntity.getOutBusinessNo());

        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setTopic(taskEntity.getTopic());
        task.setMessageId(taskEntity.getMessageId());
        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
        task.setState(taskEntity.getState().getCode());

        RLock lock = redisService.getLock(Constants.RedisKey.USER_CREDIT_ACCOUNT_LOCK + userId + "_" + creditOrderEntity.getOutBusinessNo());
        try {
            lock.lock(30, TimeUnit.SECONDS);
            idbRouterStrategy.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    //保存账户积分
                    UserCreditAccount creditAccount = userCreditAccountDao.queryUserCreditAccount(userCreditAccount);
                    if(creditAccount == null) {
                        userCreditAccountDao.insert(userCreditAccount);
                    }else {
                        userCreditAccountDao.updateAddAmount(userCreditAccount);
                    }
                    //保存账户订单
                    userCreditOrderDao.insert(userCreditOrder);
                }catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("调整账户积分额度异常，唯一索引冲突 userId:{} orderId:{}", userId, creditOrderEntity.getOrderId(), e);
                }catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("调整账户积分额度失败 userId:{} orderId:{}", userId, creditOrderEntity.getOrderId(), e);
                }
                return 1;
            });
        }finally {
            idbRouterStrategy.clear();
            lock.unlock();
        }

        try {
            eventPublisher.publish(task.getTopic(), task.getMessage());
            taskDao.updateTaskSendMessageComplete(task);
            log.info("调整账户积分记录，发送MQ消息完成 userId: {} orderId:{} topic: {}", userId, creditOrderEntity.getOrderId(), task.getTopic());
        }catch (Exception e) {
            log.error("调整账户积分记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
            taskDao.updateTaskSendMessageFail(task);
        }
    }

    @Override
    public CreditAccountEntity queryUserCreditAccount(String userId) {
        UserCreditAccount userCreditAccountReq = new UserCreditAccount();
        userCreditAccountReq.setUserId(userId);
        UserCreditAccount userCreditAccount = userCreditAccountDao.queryUserCreditAccount(userCreditAccountReq);
        if(userCreditAccount == null) return null;
        return CreditAccountEntity.builder()
                .userId(userId)
                .adjustAmount(userCreditAccount.getAvailableAmount())
                .build();
    }
}
