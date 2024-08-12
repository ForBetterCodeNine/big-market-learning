package com.project.domain.task.service;


import com.project.domain.task.model.entity.TaskEntity;

import java.util.List;

public interface ITaskService {
    /**
     * 查询发送MQ失败和超时一分钟未发送的消息
     */
    List<TaskEntity> queryNoSendMessageTaskList();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);
}
