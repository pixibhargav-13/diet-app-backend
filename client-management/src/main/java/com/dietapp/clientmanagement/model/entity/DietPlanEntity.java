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
@Table(name = "diet_plans")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class DietPlanEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_calories")
    private Integer targetCalories;

    @Column(name = "target_protein_g", precision = 7, scale = 2)
    private BigDecimal targetProteinG;

    @Column(name = "target_carbs_g", precision = 7, scale = 2)
    private BigDecimal targetCarbsG;

    @Column(name = "target_fat_g", precision = 7, scale = 2)
    private BigDecimal targetFatG;
}
