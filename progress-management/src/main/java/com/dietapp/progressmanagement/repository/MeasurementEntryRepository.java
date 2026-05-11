package com.dietapp.progressmanagement.repository;

import com.dietapp.progressmanagement.model.entity.MeasurementEntryEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.time.LocalDate;
import java.util.List;

public interface MeasurementEntryRepository extends CoreRepository<MeasurementEntryEntity, Long> {
    List<MeasurementEntryEntity> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);
    List<MeasurementEntryEntity> findByUserIdOrderByDateAsc(Long userId);
}
