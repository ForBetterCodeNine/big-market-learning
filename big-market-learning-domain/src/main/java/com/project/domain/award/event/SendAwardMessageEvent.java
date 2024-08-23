package com.project.domain.award.event;

import com.project.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SendAwardMessageEvent extends BaseEvent<SendAwardMessageEvent.SendAwardMessage> {

    @Override
    public EventMessage<SendAwardMessage> buildEventMessage(SendAwardMessage data) {
        return EventMessage.<SendAwardMessage>builder()
                .id(RandomStringUtils.randomNumeric(11))
                .timeStamp(new Date())
                .data(data)
                .build();
    }

    @Override
    public String topic() {
        return "send_award";
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SendAwardMessage {
        /**
         * 用户ID
         */
        private String userId;
        /**
         * 订单ID
         */
        private String orderId;
        /**
         * 奖品ID
         */
        private Integer awardId;
        /**
         * 奖品标题（名称）
         */
        private String awardTitle;
        /**
         * 奖品配置信息
         */
        private String awardConfig;
    }
}
