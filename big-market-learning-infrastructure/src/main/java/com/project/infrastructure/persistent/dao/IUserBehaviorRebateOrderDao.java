package com.project.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.project.infrastructure.persistent.po.UserBehaviorRebateOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserBehaviorRebateOrderDao {

    void insert(UserBehaviorRebateOrder userBehaviorRebateOrder);
}
