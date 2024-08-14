package com.project.domain.activity.service.partake;

import com.project.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.project.domain.activity.model.entity.*;
import com.project.domain.activity.model.valobj.UserRaffleOrderStateVO;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;


@Service
@Slf4j
public class DefaultRaffleActivityPartake extends AbstractRaffleActivityPartake{

    private final SimpleDateFormat dateFormatMonth = new SimpleDateFormat("yyyy-MM");
    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");

    public DefaultRaffleActivityPartake(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    protected CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date date) {

        String month = dateFormatMonth.format(date);
        String day = dateFormatDay.format(date);

        CreatePartakeOrderAggregate partakeOrderAggregate = new CreatePartakeOrderAggregate();
        partakeOrderAggregate.setActivityId(activityId);
        partakeOrderAggregate.setUserId(userId);

        //根据信息查询账户总额度
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccountByUserIdAndAcId(userId, activityId);
        if(activityAccountEntity == null || activityAccountEntity.getTotalCountSurplus() <= 0) {
            log.info("用户{}总额度不足！！！", userId);
            throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
        }

        partakeOrderAggregate.setActivityAccountEntity(activityAccountEntity);

        //查询账户月额度
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, month);
        if(activityAccountMonthEntity != null && activityAccountMonthEntity.getMonthCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
        }

        //创建月额度
        boolean isExistAccountMonth = null != activityAccountMonthEntity;
        if(null == activityAccountMonthEntity) {
            activityAccountMonthEntity = new ActivityAccountMonthEntity();
            activityAccountMonthEntity.setUserId(userId);
            activityAccountMonthEntity.setActivityId(activityId);
            activityAccountMonthEntity.setMonth(month);
            activityAccountMonthEntity.setMonthCount(activityAccountEntity.getMonthCount());
            activityAccountMonthEntity.setMonthCountSurplus(activityAccountEntity.getMonthCount());
        }
        partakeOrderAggregate.setExistAccountMonth(isExistAccountMonth);
        partakeOrderAggregate.setActivityAccountMonthEntity(activityAccountMonthEntity);

        //查询账户日额度
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, day);
        if(activityAccountDayEntity != null && activityAccountDayEntity.getDayCountSurplus() <= 0) {
            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
        }
        //创建日额度
        boolean isExistAccountDay = null != activityAccountDayEntity;
        if(activityAccountDayEntity == null) {
            activityAccountDayEntity = new ActivityAccountDayEntity();
            activityAccountDayEntity.setUserId(userId);
            activityAccountDayEntity.setActivityId(activityId);
            activityAccountDayEntity.setDay(day);
            activityAccountDayEntity.setDayCount(activityAccountEntity.getDayCount());
            activityAccountDayEntity.setDayCountSurplus(activityAccountEntity.getDayCount());
        }
        partakeOrderAggregate.setExistAccountDay(isExistAccountDay);
        partakeOrderAggregate.setActivityAccountDayEntity(activityAccountDayEntity);

        return partakeOrderAggregate;
    }

    @Override
    protected UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date date) {
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityEntityByAcId(activityId);
        UserRaffleOrderEntity entity = new UserRaffleOrderEntity();
        entity.setUserId(userId);
        entity.setActivityId(activityId);
        entity.setOrderId(RandomStringUtils.randomNumeric(12));
        entity.setActivityName(activityEntity.getActivityName());
        entity.setOrderTime(date);
        entity.setOrderState(UserRaffleOrderStateVO.create);
        entity.setStrategyId(activityEntity.getStrategyId());
        entity.setEndDateTime(activityEntity.getEndDateTime());
        return entity;
    }
}
