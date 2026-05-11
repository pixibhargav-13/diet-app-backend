package com.dietapp.nutritionmanagement.repository;

import com.dietapp.nutritionmanagement.model.entity.ClientMealPlanAssignmentEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.Optional;

public interface ClientMealPlanAssignmentRepository extends CoreRepository<ClientMealPlanAssignmentEntity, Long> {
    Optional<ClientMealPlanAssignmentEntity> findTopByClientIdOrderByAssignedAtDesc(Long clientId);
    void deleteByClientId(Long clientId);
}
