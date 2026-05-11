package com.dietapp.sessionmanagement.repository;

import com.dietapp.sessionmanagement.model.entity.SessionEntity;
import com.dietapp.usermanagement.repository.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends CoreRepository<SessionEntity, Long> {

    List<SessionEntity> findByClientIdOrderBySessionDateDesc(Long clientId);

    List<SessionEntity> findAllByOrderBySessionDateDesc();

    @Query("SELECT COUNT(s) FROM SessionEntity s WHERE s.clientId = :clientId AND s.status = :status")
    long countByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") String status);

    @Query("SELECT COUNT(s) FROM SessionEntity s WHERE s.clientId = :clientId")
    long countByClientId(@Param("clientId") Long clientId);
}
