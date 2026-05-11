package com.dietapp.shopmanagement.repository;

import com.dietapp.shopmanagement.model.entity.WishlistEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends CoreRepository<WishlistEntity, Long> {
    List<WishlistEntity> findByUserId(Long userId);
    Optional<WishlistEntity> findByUserIdAndProductId(Long userId, Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
