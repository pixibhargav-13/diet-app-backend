package com.dietapp.progressmanagement.repository;

import com.dietapp.progressmanagement.model.entity.WeightGoalEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.Optional;

public interface WeightGoalRepository extends CoreRepository<WeightGoalEntity, Long> {
    Optional<WeightGoalEntity> findByUserId(Long userId);
}
