package com.project.domain.rebate.event;

import com.project.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SendRebateMessageEvent extends BaseEvent<SendRebateMessageEvent.RebateMessage> {

    @Override
    public EventMessage<RebateMessage> buildEventMessage(RebateMessage data) {
        return EventMessage.<RebateMessage>builder()
                .id(RandomStringUtils.randomNumeric(11))
                .timeStamp(new Date())
                .data(data)
                .build();
    }

    @Override
    public String topic() {
        return "big-market-daily-rebate-topic";
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RebateMessage {
        private String userId;

        private String rebateDesc;

        private String rebateType;

        private String rebateConfig;

        private String bizId;
    }
}
