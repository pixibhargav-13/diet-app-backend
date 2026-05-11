package com.dietapp.sessionmanagement.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reschedule_history")
public class RescheduleHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "from_time", nullable = false, length = 10)
    private String fromTime;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "to_time", nullable = false, length = 10)
    private String toTime;

    @Column(name = "rescheduled_at", nullable = false)
    @Builder.Default
    private Instant rescheduledAt = Instant.now();
}
