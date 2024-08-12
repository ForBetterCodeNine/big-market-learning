package com.project.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.project.infrastructure.persistent.po.RaffleActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityAccountDao {
    int updateAccountQuota(RaffleActivityAccount activityAccount);

    void insert(RaffleActivityAccount activityAccount);

    @DBRouter
    RaffleActivityAccount queryActivityAccountByUserIdAndAcId(RaffleActivityAccount activityAccount);

    int updateActivityAccountSubtractionQuota(RaffleActivityAccount activityAccount);

    void updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount activityAccount);

    void updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount activityAccount);

    int updateActivityAccountMonthSubtractionQuota(RaffleActivityAccount raffleActivityAccount);

    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccount raffleActivityAccount);
}
