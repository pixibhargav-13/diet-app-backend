package com.dietapp.clientmanagement.repository;

import com.dietapp.clientmanagement.model.entity.ClientNoteEntity;
import com.dietapp.usermanagement.repository.CoreRepository;

import java.util.List;

public interface ClientNoteRepository extends CoreRepository<ClientNoteEntity, Long> {
    List<ClientNoteEntity> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
