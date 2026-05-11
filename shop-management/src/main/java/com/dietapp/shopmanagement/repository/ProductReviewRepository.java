package com.dietapp.shopmanagement.repository;

import com.dietapp.shopmanagement.model.entity.ProductReviewEntity;
import com.dietapp.usermanagement.repository.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends CoreRepository<ProductReviewEntity, Long> {
    List<ProductReviewEntity> findByProductIdOrderByCreatedAtDesc(Long productId);
    Optional<ProductReviewEntity> findByProductIdAndUserId(Long productId, Long userId);

    @Query("SELECT AVG(r.rating) FROM ProductReviewEntity r WHERE r.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
}
