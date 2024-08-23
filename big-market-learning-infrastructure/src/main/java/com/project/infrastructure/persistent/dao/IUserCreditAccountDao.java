package com.project.infrastructure.persistent.dao;

import com.project.infrastructure.persistent.po.UserCreditAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserCreditAccountDao {

    void insert(UserCreditAccount userCreditAccount);

    int updateAddAmount(UserCreditAccount userCreditAccount);
}
