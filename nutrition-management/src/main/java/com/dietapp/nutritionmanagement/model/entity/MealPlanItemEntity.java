package com.dietapp.nutritionmanagement.model.entity;

import com.dietapp.usermanagement.model.AuditInfo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meal_plan_items")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class MealPlanItemEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlanEntity mealPlan;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(precision = 8, scale = 2)
    private BigDecimal kcal;

    @Column(precision = 7, scale = 2)
    private BigDecimal protein;

    @Column(precision = 7, scale = 2)
    private BigDecimal carbs;

    @Column(precision = 7, scale = 2)
    private BigDecimal fat;

    @Column(precision = 7, scale = 2)
    private BigDecimal fiber;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(name = "prep_notes", columnDefinition = "TEXT")
    private String prepNotes;

    @Column(name = "scheduled_time", length = 20)
    private String scheduledTime;
}
