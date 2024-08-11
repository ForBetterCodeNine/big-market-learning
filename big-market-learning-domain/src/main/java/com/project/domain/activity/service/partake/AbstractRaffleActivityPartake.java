package com.project.domain.activity.service.partake;

import com.alibaba.fastjson.JSON;
import com.project.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import com.project.domain.activity.model.entity.ActivityEntity;
import com.project.domain.activity.model.entity.PartakeRaffleActivityEntity;
import com.project.domain.activity.model.entity.UserRaffleOrderEntity;
import com.project.domain.activity.model.valobj.ActivityStateVO;
import com.project.domain.activity.repository.IActivityRepository;
import com.project.domain.activity.service.IRaffleActivityPartakeService;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
public abstract class AbstractRaffleActivityPartake implements IRaffleActivityPartakeService {

    protected final IActivityRepository activityRepository;

    public AbstractRaffleActivityPartake(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        String userId = partakeRaffleActivityEntity.getUserId();
        Long activityId = partakeRaffleActivityEntity.getActivityId();
        Date date = new Date();
        //查询活动状态等信息
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityEntityByAcId(activityId);

        if(!ActivityStateVO.open.equals(activityEntity.getState())) {
            log.info("活动{} 没有开放！！！", activityEntity.getActivityName());
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(), ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }
        //校验活动时间 是否开始 和是否过期
        if(date.after(activityEntity.getEndDateTime()) || date.before(activityEntity.getBeginDateTime())) {
            log.info("活动{} 已经结束或者未开始！！！", activityEntity.getActivityName());
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }
        //查询当前用户下未被使用的参与订单记录
        UserRaffleOrderEntity userRaffleOrderEntity = activityRepository.queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
        if(userRaffleOrderEntity != null) {
            log.info("创建参与活动订单 userId:{} activityId:{} userRaffleOrderEntity:{}", userId, activityId, JSON.toJSONString(userRaffleOrderEntity));
            return userRaffleOrderEntity;
        }
        //判断用户额度是否足够
        CreatePartakeOrderAggregate partakeOrderAggregate = this.doFilterAccount(userId, activityId, date);

        //构建订单
        UserRaffleOrderEntity userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, date);

        //填充抽奖蛋实体对象
        partakeOrderAggregate.setUserRaffleOrderEntity(userRaffleOrder);

        //保存聚合对象
        activityRepository.saveCreatePartakeOrderAggregate(partakeOrderAggregate);

        return userRaffleOrder;
    }

    protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date date);

    protected abstract UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date date);
}
