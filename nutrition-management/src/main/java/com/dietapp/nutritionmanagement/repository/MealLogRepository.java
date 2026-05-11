package com.dietapp.nutritionmanagement.repository;

import com.dietapp.nutritionmanagement.model.entity.MealLogEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.time.LocalDate;
import java.util.List;

public interface MealLogRepository extends CoreRepository<MealLogEntity, Long> {
    List<MealLogEntity> findByUserIdAndDateOrderByCreatedAtAsc(Long userId, LocalDate date);
    long countByUserIdAndDate(Long userId, LocalDate date);
}
