package com.project.domain.rebate.service;

import com.project.domain.rebate.event.SendRebateMessageEvent;
import com.project.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import com.project.domain.rebate.model.entity.BehaviorEntity;
import com.project.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import com.project.domain.rebate.model.entity.TaskEntity;
import com.project.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import com.project.domain.rebate.model.valobj.TaskStateVO;
import com.project.domain.rebate.repository.IBehaviorRebateRepository;
import com.project.types.event.BaseEvent;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
public class BehaviorRebateService implements IBehaviorRebateService{

    @Resource
    private IBehaviorRebateRepository behaviorRebateRepository;

    @Resource
    private SendRebateMessageEvent sendRebateMessageEvent;

    @Override
    public List<String> createOrder(BehaviorEntity behaviorEntity) {
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOList = behaviorRebateRepository.queryDailyBehaviorRebateConfigs(behaviorEntity.getBehaviorTypeVO());
        //构建聚合对象
        List<String> orderIds = new ArrayList<>();
        List<BehaviorRebateAggregate> behaviorRebateAggregates = new ArrayList<>();
        for(DailyBehaviorRebateVO dailyBehaviorRebateVO : dailyBehaviorRebateVOList) {
            String bizId = behaviorEntity.getUserId() + "_" + dailyBehaviorRebateVO.getBehaviorType() + "_" + behaviorEntity.getOutBusinessNo();
            BehaviorRebateOrderEntity behaviorRebateOrderEntity = new BehaviorRebateOrderEntity();
            behaviorRebateOrderEntity.setUserId(behaviorEntity.getUserId());
            behaviorRebateOrderEntity.setBizId(bizId);
            behaviorRebateOrderEntity.setOrderId(RandomStringUtils.randomNumeric(11));
            behaviorRebateOrderEntity.setBehaviorType(dailyBehaviorRebateVO.getBehaviorType());
            behaviorRebateOrderEntity.setRebateDesc(dailyBehaviorRebateVO.getRebateDesc());
            behaviorRebateOrderEntity.setRebateConfig(dailyBehaviorRebateVO.getRebateConfig());
            orderIds.add(behaviorRebateOrderEntity.getOrderId());

            //MQ消息对象
            SendRebateMessageEvent.RebateMessage rebateMessage = SendRebateMessageEvent.RebateMessage.builder()
                    .bizId(bizId)
                    .userId(behaviorEntity.getUserId())
                    .rebateConfig(dailyBehaviorRebateVO.getRebateConfig())
                    .rebateDesc(dailyBehaviorRebateVO.getRebateDesc())
                    .rebateType(dailyBehaviorRebateVO.getRebateType())
                    .build();

            //构建事件消息
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage = sendRebateMessageEvent.buildEventMessage(rebateMessage);

            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setMessage(rebateMessageEventMessage);
            taskEntity.setState(TaskStateVO.create);
            taskEntity.setTopic(sendRebateMessageEvent.topic());
            taskEntity.setMessageId(rebateMessageEventMessage.getId());
            taskEntity.setUserId(behaviorEntity.getUserId());

            BehaviorRebateAggregate behaviorRebateAggregate = new BehaviorRebateAggregate();
            behaviorRebateAggregate.setTaskEntity(taskEntity);
            behaviorRebateAggregate.setUserId(behaviorEntity.getUserId());
            behaviorRebateAggregate.setBehaviorRebateOrderEntity(behaviorRebateOrderEntity);

            behaviorRebateAggregates.add(behaviorRebateAggregate);
        }

        //存储聚合对象
        behaviorRebateRepository.saveUserRebateRecord(behaviorEntity.getUserId(), behaviorRebateAggregates);
        return orderIds;
    }
}
