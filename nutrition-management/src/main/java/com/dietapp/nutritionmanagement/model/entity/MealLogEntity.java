package com.dietapp.nutritionmanagement.model.entity;

import com.dietapp.usermanagement.model.AuditInfo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meal_logs")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class MealLogEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "meal_name", nullable = false, length = 200)
    private String mealName;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal kcal;

    @Column(precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal protein = BigDecimal.ZERO;

    @Column(precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal carbs = BigDecimal.ZERO;

    @Column(precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal fat = BigDecimal.ZERO;

    @Column(precision = 7, scale = 2)
    @Builder.Default
    private BigDecimal fiber = BigDecimal.ZERO;

    @Column(length = 20)
    @Builder.Default
    private String source = "custom";

    @Column(name = "photo_url", length = 500)
    private String photoUrl;
}
