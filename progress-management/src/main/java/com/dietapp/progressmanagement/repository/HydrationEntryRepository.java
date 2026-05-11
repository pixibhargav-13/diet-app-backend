package com.dietapp.progressmanagement.repository;

import com.dietapp.progressmanagement.model.entity.HydrationEntryEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.time.LocalDate;
import java.util.List;

public interface HydrationEntryRepository extends CoreRepository<HydrationEntryEntity, Long> {
    List<HydrationEntryEntity> findByUserIdAndDateOrderByLoggedAtAsc(Long userId, LocalDate date);
    List<HydrationEntryEntity> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);
}
