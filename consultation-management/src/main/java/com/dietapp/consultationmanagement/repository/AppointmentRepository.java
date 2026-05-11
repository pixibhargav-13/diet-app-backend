package com.dietapp.consultationmanagement.repository;

import com.dietapp.consultationmanagement.model.entity.AppointmentEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.List;

public interface AppointmentRepository extends CoreRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByClientIdOrderByAppointmentDateDesc(Long clientId);
    List<AppointmentEntity> findByClientIdAndStatusOrderByAppointmentDateDesc(Long clientId, String status);
    List<AppointmentEntity> findAllByOrderByAppointmentDateDesc();
    List<AppointmentEntity> findByStatusOrderByAppointmentDateDesc(String status);
    List<AppointmentEntity> findByClientIdAndStatusInOrderByAppointmentDateDesc(Long clientId, List<String> statuses);
}
