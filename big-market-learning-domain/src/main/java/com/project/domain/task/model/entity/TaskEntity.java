package com.project.domain.task.model.entity;

import lombok.Data;

@Data
public class TaskEntity {
    private String userId;

    private String topic;

    private String messageId;

    private String message;
}
