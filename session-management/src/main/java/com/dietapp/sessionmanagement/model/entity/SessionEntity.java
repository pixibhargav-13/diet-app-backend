package com.dietapp.sessionmanagement.model.entity;

import com.dietapp.usermanagement.model.AuditInfo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sessions")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class SessionEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session_time", nullable = false, length = 10)
    private String sessionTime;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 60;

    @Column(name = "repeat_type", nullable = false, length = 20)
    @Builder.Default
    private String repeatType = "none";

    @Column(name = "meet_link", length = 500)
    private String meetLink;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "scheduled";

    @Column(name = "agreement_accepted", nullable = false)
    @Builder.Default
    private Boolean agreementAccepted = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
