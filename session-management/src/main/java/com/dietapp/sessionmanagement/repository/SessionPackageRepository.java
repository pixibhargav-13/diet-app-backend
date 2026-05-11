package com.dietapp.sessionmanagement.repository;

import com.dietapp.sessionmanagement.model.entity.SessionPackageEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.List;

public interface SessionPackageRepository extends CoreRepository<SessionPackageEntity, Long> {

    List<SessionPackageEntity> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<SessionPackageEntity> findByClientIdAndActiveTrue(Long clientId);

    List<SessionPackageEntity> findAllByOrderByCreatedAtDesc();
}
