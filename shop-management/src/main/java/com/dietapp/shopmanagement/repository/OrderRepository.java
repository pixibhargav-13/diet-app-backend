package com.dietapp.shopmanagement.repository;

import com.dietapp.shopmanagement.model.entity.OrderEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.List;

public interface OrderRepository extends CoreRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<OrderEntity> findAllByOrderByCreatedAtDesc();
    List<OrderEntity> findByStatusOrderByCreatedAtDesc(String status);
}
