package com.project.infrastructure.persistent.dao;

import com.project.infrastructure.persistent.po.RaffleActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRaffleActivityAccountDao {
    int updateAccountQuota(RaffleActivityAccount activityAccount);

    void insert(RaffleActivityAccount activityAccount);
}
