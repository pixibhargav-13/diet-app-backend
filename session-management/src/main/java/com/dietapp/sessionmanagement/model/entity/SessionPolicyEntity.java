package com.dietapp.sessionmanagement.model.entity;

import com.dietapp.usermanagement.model.AuditInfo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "session_policies")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class SessionPolicyEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null means this is the global default policy. */
    @Column(name = "client_id", unique = true)
    private Long clientId;

    @Column(name = "cancellation_notice_hours", nullable = false)
    @Builder.Default
    private Integer cancellationNoticeHours = 24;

    @Column(name = "max_reschedule_per_month", nullable = false)
    @Builder.Default
    private Integer maxReschedulePerMonth = 2;
}
