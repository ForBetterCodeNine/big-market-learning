package com.project.domain.activity.service;

import com.project.domain.activity.repository.IActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class RaffleActivityService extends AbstractActivityService{
    public RaffleActivityService(IActivityRepository activityRepository) {
        super(activityRepository);
    }
}
