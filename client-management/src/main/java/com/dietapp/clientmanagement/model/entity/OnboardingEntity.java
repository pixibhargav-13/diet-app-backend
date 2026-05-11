package com.dietapp.clientmanagement.model.entity;

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
@Table(name = "onboarding_profiles")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class OnboardingEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "health_conditions", columnDefinition = "TEXT")
    private String healthConditions;

    @Column(length = 500)
    private String allergies;

    @Column(name = "dietary_preference", length = 30)
    private String dietaryPreference;

    @Column(length = 200)
    private String goal;

    private Integer age;

    @Column(length = 10)
    private String gender;

    @Column(name = "height_cm", precision = 6, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "bmi_value", precision = 5, scale = 2)
    private BigDecimal bmiValue;

    @Column(name = "bmi_category", length = 50)
    private String bmiCategory;

    @Column(nullable = false)
    @Builder.Default
    private Boolean complete = false;
}
