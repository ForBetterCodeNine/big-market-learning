package com.project.domain.credit.model.aggregate;

import com.project.domain.credit.model.entity.CreditAccountEntity;
import com.project.domain.credit.model.entity.CreditOrderEntity;
import com.project.domain.credit.model.entity.TaskEntity;
import com.project.domain.credit.model.event.CreditAdjustSuccessMessageEvent;
import com.project.domain.credit.model.valobj.TradeNameVO;
import com.project.domain.credit.model.valobj.TradeTypeVO;
import com.project.domain.rebate.model.valobj.TaskStateVO;
import com.project.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;

import java.math.BigDecimal;

/**
 * 交易聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeAggregate {
    private String userId;

    private CreditAccountEntity creditAccountEntity;

    private CreditOrderEntity creditOrderEntity;

    // 任务实体 - 补偿 MQ 消息
    private TaskEntity taskEntity;

    public static CreditAccountEntity createCreditAccountEntity(String userId, BigDecimal adjustAmount) {
        return CreditAccountEntity.builder().userId(userId).adjustAmount(adjustAmount).build();
    }

    public static CreditOrderEntity createCreditOrderEntity(String userId, TradeNameVO tradeNameVO, TradeTypeVO tradeTypeVO, BigDecimal bigDecimal,
                                                            String outBusinessNo) {
        return CreditOrderEntity.builder()
                .userId(userId)
                .tradeName(tradeNameVO)
                .tradeType(tradeTypeVO)
                .tradeAmount(bigDecimal)
                .orderId(RandomStringUtils.randomNumeric(12))
                .outBusinessNo(outBusinessNo)
                .build();

    }

    public static TaskEntity createTaskEntity(String userId, String topic, String messageId, BaseEvent.EventMessage<CreditAdjustSuccessMessageEvent.CreditAdjustSuccessMessage> message) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUserId(userId);
        taskEntity.setTopic(topic);
        taskEntity.setMessageId(messageId);
        taskEntity.setMessage(message);
        taskEntity.setState(TaskStateVO.create);
        return taskEntity;
    }
}
