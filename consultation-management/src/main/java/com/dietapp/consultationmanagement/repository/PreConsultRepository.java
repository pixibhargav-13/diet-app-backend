package com.dietapp.consultationmanagement.repository;

import com.dietapp.consultationmanagement.model.entity.PreConsultEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.Optional;

public interface PreConsultRepository extends CoreRepository<PreConsultEntity, Long> {
    Optional<PreConsultEntity> findByAppointmentId(Long appointmentId);
}
