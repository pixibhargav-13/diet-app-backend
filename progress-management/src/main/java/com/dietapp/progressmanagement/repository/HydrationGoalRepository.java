package com.dietapp.progressmanagement.repository;

import com.dietapp.progressmanagement.model.entity.HydrationGoalEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.Optional;

public interface HydrationGoalRepository extends CoreRepository<HydrationGoalEntity, Long> {
    Optional<HydrationGoalEntity> findByUserId(Long userId);
}
