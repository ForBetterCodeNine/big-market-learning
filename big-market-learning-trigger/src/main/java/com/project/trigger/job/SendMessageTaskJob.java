package com.project.trigger.job;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.project.domain.task.model.entity.TaskEntity;
import com.project.domain.task.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component()
public class SendMessageTaskJob {
    @Resource
    private ITaskService taskService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Scheduled(cron = "0/5 * * * * ?")
    public void exec() {
        try {
            int dbCount = dbRouter.dbCount();
            for(int i=1;i<=dbCount;i++) {
                int finalDbIdx = i;
                threadPoolExecutor.execute(() ->{
                    try {
                        dbRouter.setDBKey(finalDbIdx);
                        dbRouter.setTBKey(0);
                        List<TaskEntity> taskEntityList = taskService.queryNoSendMessageTaskList();
                        if(taskEntityList.isEmpty()) return;
                        for(TaskEntity entity:taskEntityList) {
                            threadPoolExecutor.execute(() ->{
                                try {
                                    taskService.sendMessage(entity);
                                    taskService.updateTaskSendMessageCompleted(entity.getUserId(), entity.getMessageId());
                                }catch (Exception e) {
                                    log.error("定时任务，发送MQ消息失败 userId: {} topic: {}", entity.getUserId(), entity.getTopic());
                                    taskService.updateTaskSendMessageFail(entity.getUserId(), entity.getMessageId());
                                }
                            });
                        }
                    }finally {
                        dbRouter.clear();
                    }
                });
            }
        }catch (Exception e) {
            log.error("定时任务，扫描MQ任务表发送消息失败。", e);
        }finally {
            dbRouter.clear();
        }
    }
}
