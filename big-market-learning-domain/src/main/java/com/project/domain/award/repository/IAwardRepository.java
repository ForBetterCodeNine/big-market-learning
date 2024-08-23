package com.project.domain.award.repository;

import com.project.domain.award.model.aggregate.GiveoutPrizesAggregate;
import com.project.domain.award.model.aggregate.UserAwardRecordAggregate;

public interface IAwardRepository {
    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

    String queryAwardConfig(Integer awardId);

    void saveGiveoutPrizesAggregate(GiveoutPrizesAggregate giveoutPrizesAggregate);

    String queryAwardKey(Integer awardId);
}
