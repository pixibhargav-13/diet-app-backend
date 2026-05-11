package com.dietapp.consultationmanagement.model.entity;

import com.dietapp.usermanagement.model.AuditInfo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pre_consult_forms")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class PreConsultEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false, unique = true)
    private Long appointmentId;

    @Column(name = "current_concerns", columnDefinition = "TEXT")
    private String currentConcerns;

    @Column(name = "recent_diet_changes", columnDefinition = "TEXT")
    private String recentDietChanges;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    @Column(name = "lab_report_urls", columnDefinition = "TEXT")
    private String labReportUrls;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "submitted_at")
    @Builder.Default
    private Instant submittedAt = Instant.now();
}
