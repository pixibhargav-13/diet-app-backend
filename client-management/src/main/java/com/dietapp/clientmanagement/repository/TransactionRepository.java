package com.dietapp.clientmanagement.repository;

import com.dietapp.clientmanagement.model.entity.TransactionEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends CoreRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);
    List<TransactionEntity> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(String type, Instant from, Instant to);
    List<TransactionEntity> findAllByOrderByCreatedAtDesc();
}
