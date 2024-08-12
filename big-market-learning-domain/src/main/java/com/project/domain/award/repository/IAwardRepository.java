package com.project.domain.award.repository;

import com.project.domain.award.model.aggregate.UserAwardRecordAggregate;

public interface IAwardRepository {
    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);
}
