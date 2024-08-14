package com.project.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.project.infrastructure.persistent.po.RaffleActivityAccountMonth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityAccountMonthDao {

    @DBRouter
    RaffleActivityAccountMonth queryActivityAccountMonth(RaffleActivityAccountMonth activityAccountMonth);

    int updateActivityAccountMonthSubstractionQuota(RaffleActivityAccountMonth activityAccountMonth);

    void insertActivityAccountMonth(RaffleActivityAccountMonth activityAccountMonth);

    void addAccountQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);
}
