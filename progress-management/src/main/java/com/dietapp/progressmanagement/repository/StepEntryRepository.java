package com.dietapp.progressmanagement.repository;

import com.dietapp.progressmanagement.model.entity.StepEntryEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepEntryRepository extends CoreRepository<StepEntryEntity, Long> {
    List<StepEntryEntity> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);
    List<StepEntryEntity> findByUserIdOrderByDateAsc(Long userId);
    Optional<StepEntryEntity> findByUserIdAndDate(Long userId, LocalDate date);
}
