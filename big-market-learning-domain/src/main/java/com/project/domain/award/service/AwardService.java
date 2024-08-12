package com.project.domain.award.service;


import com.project.domain.award.event.SendAwardMessageEvent;
import com.project.domain.award.model.aggregate.UserAwardRecordAggregate;
import com.project.domain.award.model.entity.TaskEntity;
import com.project.domain.award.model.entity.UserAwardRecordEntity;
import com.project.domain.award.model.valobj.TaskStateVO;
import com.project.domain.award.repository.IAwardRepository;
import com.project.types.event.BaseEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AwardService implements IAwardService{

    @Resource
    private IAwardRepository awardRepository;

    @Resource
    private SendAwardMessageEvent sendAwardMessageEvent;

    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
        //构建消息对象
        SendAwardMessageEvent.SendAwardMessage sendAwardMessage = new SendAwardMessageEvent.SendAwardMessage();
        sendAwardMessage.setAwardId(userAwardRecordEntity.getAwardId());
        sendAwardMessage.setUserId(userAwardRecordEntity.getUserId());
        sendAwardMessage.setAwardTitle(userAwardRecordEntity.getAwardTitle());

        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEventMessage(sendAwardMessage);

        //构建任务对象
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUserId(userAwardRecordEntity.getUserId());
        taskEntity.setTopic(sendAwardMessageEvent.topic());
        taskEntity.setMessageId(sendAwardMessageEventMessage.getId());
        taskEntity.setMessage(sendAwardMessageEventMessage);
        taskEntity.setState(TaskStateVO.create);

        //构建聚合对象
        UserAwardRecordAggregate userAwardRecordAggregate = new UserAwardRecordAggregate();
        userAwardRecordAggregate.setUserAwardRecordEntity(userAwardRecordEntity);
        userAwardRecordAggregate.setTaskEntity(taskEntity);

        //存储聚合对象 一个事务下 用户的中奖记录
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
    }
}
