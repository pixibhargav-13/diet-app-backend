package com.dietapp.sessionmanagement.repository;

import com.dietapp.sessionmanagement.model.entity.SessionPolicyEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.List;
import java.util.Optional;

public interface SessionPolicyRepository extends CoreRepository<SessionPolicyEntity, Long> {

    /** Returns the global policy (clientId IS NULL). */
    Optional<SessionPolicyEntity> findByClientIdIsNull();

    /** Returns the client-specific override if it exists. */
    Optional<SessionPolicyEntity> findByClientId(Long clientId);

    /** Returns all client-specific overrides (excludes global). */
    List<SessionPolicyEntity> findByClientIdIsNotNull();
}
