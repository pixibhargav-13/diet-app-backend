package com.dietapp.sessionmanagement.model.entity;

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
@Table(name = "session_packages")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class SessionPackageEntity extends AuditInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "package_name", nullable = false, length = 200)
    private String packageName;

    @Column(name = "session_count", nullable = false)
    private Integer sessionCount;

    @Column(name = "sessions_used", nullable = false)
    @Builder.Default
    private Integer sessionsUsed = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
