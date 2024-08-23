package com.project.domain.award.model.aggregate;

import com.project.domain.award.model.entity.UserAwardRecordEntity;
import com.project.domain.award.model.entity.UserCreditAwardEntity;
import com.project.domain.award.model.valobj.AwardStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 发奖奖品聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GiveoutPrizesAggregate {

    private String userId;

    //用户发奖记录
    private UserAwardRecordEntity userAwardRecordEntity;

    //用户积分奖品
    private UserCreditAwardEntity userCreditAwardEntity;

    public static UserAwardRecordEntity buildDistributeUserAwardRecordEntity(String userId, String orderId, Integer awardId, AwardStateVO awardState) {
        UserAwardRecordEntity userAwardRecord = new UserAwardRecordEntity();
        userAwardRecord.setUserId(userId);
        userAwardRecord.setOrderId(orderId);
        userAwardRecord.setAwardId(awardId);
        userAwardRecord.setState(awardState);
        return userAwardRecord;
    }

    public static UserCreditAwardEntity buildUserCreditAwardEntity(String userId, BigDecimal bigDecimal) {
        return UserCreditAwardEntity.builder().userId(userId).creditAmount(bigDecimal).build();
    }

}
