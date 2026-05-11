package com.dietapp.sessionmanagement.repository;

import com.dietapp.sessionmanagement.model.entity.RescheduleHistoryEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.time.Instant;
import java.util.List;

public interface RescheduleHistoryRepository extends CoreRepository<RescheduleHistoryEntity, Long> {

    List<RescheduleHistoryEntity> findBySessionIdOrderByRescheduledAtDesc(Long sessionId);

    long countBySessionIdAndRescheduledAtAfter(Long sessionId, Instant after);
}
