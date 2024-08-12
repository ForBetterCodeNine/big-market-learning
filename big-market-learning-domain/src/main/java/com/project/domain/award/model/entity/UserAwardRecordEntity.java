package com.project.domain.award.model.entity;

import com.project.domain.award.model.valobj.AwardStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户中奖记录实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAwardRecordEntity {
    private String userId;

    private Long activityId;

    private Long strategyId;

    private String orderId;

    private Integer awardId;

    private String awardTitle;

    private Date awardTime;

    private AwardStateVO state;
}
