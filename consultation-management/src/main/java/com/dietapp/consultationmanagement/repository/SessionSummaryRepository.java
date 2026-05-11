package com.dietapp.consultationmanagement.repository;

import com.dietapp.consultationmanagement.model.entity.SessionSummaryEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.Optional;

public interface SessionSummaryRepository extends CoreRepository<SessionSummaryEntity, Long> {
    Optional<SessionSummaryEntity> findByAppointmentId(Long appointmentId);
}
