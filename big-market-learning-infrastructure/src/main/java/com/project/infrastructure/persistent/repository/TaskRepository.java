package com.project.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.project.domain.task.model.entity.TaskEntity;
import com.project.domain.task.repository.ITaskRepository;
import com.project.infrastructure.event.EventPublisher;
import com.project.infrastructure.persistent.dao.ITaskDao;
import com.project.infrastructure.persistent.po.Task;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TaskRepository implements ITaskRepository {

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private EventPublisher eventPublisher;


    @Override
    public List<TaskEntity> queryNoSendMessageTaskList() {
        List<TaskEntity> taskEntityList = new ArrayList<>();
        List<Task> taskList = taskDao.queryNoSendMessageTaskList();
        if(taskList == null || taskList.size() == 0) return null;
        for(Task task : taskList) {
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setUserId(task.getUserId());
            taskEntity.setTopic(task.getTopic());
            taskEntity.setMessage(task.getMessage());
            taskEntity.setMessageId(task.getMessageId());

            taskEntityList.add(taskEntity);
        }
        return taskEntityList;
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
    }

    @Override
    public void updateTaskSendMessageCompleted(String userId, String messageId) {
        Task req = new Task();
        req.setUserId(userId);
        req.setMessageId(messageId);
        taskDao.updateTaskSendMessageComplete(req);
    }

    @Override
    public void updateTaskSendMessageFail(String userId, String messageId) {
        Task req = new Task();
        req.setUserId(userId);
        req.setMessageId(messageId);
        taskDao.updateTaskSendMessageFail(req);
    }
}
