package com.dietapp.clientmanagement.repository;

import com.dietapp.clientmanagement.model.entity.ClientDietPlanAssignmentEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.Optional;

public interface ClientDietPlanAssignmentRepository extends CoreRepository<ClientDietPlanAssignmentEntity, Long> {
    Optional<ClientDietPlanAssignmentEntity> findTopByClientIdOrderByAssignedAtDesc(Long clientId);
    long countByDietPlanId(Long dietPlanId);
    void deleteByClientId(Long clientId);
}
