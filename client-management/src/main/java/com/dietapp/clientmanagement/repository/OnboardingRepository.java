package com.dietapp.clientmanagement.repository;

import com.dietapp.clientmanagement.model.entity.OnboardingEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.Optional;

public interface OnboardingRepository extends CoreRepository<OnboardingEntity, Long> {
    Optional<OnboardingEntity> findByUserId(Long userId);
}
