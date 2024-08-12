package com.project.domain.award.model.entity;

import com.project.domain.award.event.SendAwardMessageEvent;
import com.project.domain.award.model.valobj.TaskStateVO;
import com.project.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {
    private String userId;

    private String topic;

    private String messageId;

    private BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> message;

    private TaskStateVO state;
}
