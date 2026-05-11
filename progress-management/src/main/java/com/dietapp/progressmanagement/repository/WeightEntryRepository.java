package com.dietapp.progressmanagement.repository;

import com.dietapp.progressmanagement.model.entity.WeightEntryEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeightEntryRepository extends CoreRepository<WeightEntryEntity, Long> {
    List<WeightEntryEntity> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);
    List<WeightEntryEntity> findByUserIdOrderByDateAsc(Long userId);
}
