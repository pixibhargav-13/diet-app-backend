package com.dietapp.nutritionmanagement.repository;

import com.dietapp.nutritionmanagement.model.entity.FoodItemEntity;
import com.dietapp.usermanagement.repository.CoreRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FoodItemRepository extends CoreRepository<FoodItemEntity, Long> {
    List<FoodItemEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
