package com.dietapp.shopmanagement.repository;

import com.dietapp.shopmanagement.model.entity.ProductEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.List;

public interface ProductRepository extends CoreRepository<ProductEntity, Long> {
    List<ProductEntity> findByActiveTrue();
    List<ProductEntity> findByCategoryAndActiveTrue(String category);
    List<ProductEntity> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<ProductEntity> findByCategoryAndNameContainingIgnoreCaseAndActiveTrue(String category, String name);
}
