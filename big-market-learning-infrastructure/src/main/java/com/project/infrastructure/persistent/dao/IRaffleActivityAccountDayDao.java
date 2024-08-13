package com.project.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.project.infrastructure.persistent.po.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityAccountDayDao {
    @DBRouter
    RaffleActivityAccountDay queryActivityAccountDay(RaffleActivityAccountDay activityAccountDay);

    int updateActivityAccountDaySubstractionQuota(RaffleActivityAccountDay activityAccountDay);

    void insertActivityAccountDay(RaffleActivityAccountDay raffleActivityAccountDay);

    @DBRouter
    Integer queryRaffleActivityDayPartakeCount(RaffleActivityAccountDay raffleActivityAccountDay);
}
