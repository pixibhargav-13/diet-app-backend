package com.dietapp.sessionmanagement.controller;

import com.dietapp.api.sessionmanagement.api.SessionsApi;
import com.dietapp.api.sessionmanagement.model.*;
import com.dietapp.sessionmanagement.service.SessionService;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SessionController implements SessionsApi {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionResponse>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionResponse>> createSession(CreateSessionRequest createSessionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(createSessionRequest));
    }

    @Override
    public ResponseEntity<List<SessionResponse>> getMySessions() {
        return ResponseEntity.ok(sessionService.getSessionsByClientId(currentUserId()));
    }

    @Override
    public ResponseEntity<ClientSessionStats> getMyStats() {
        return ResponseEntity.ok(sessionService.getClientStats(currentUserId()));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionResponse>> getSessionsByClient(Long clientId) {
        return ResponseEntity.ok(sessionService.getSessionsByClientId(clientId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientSessionStats> getClientStats(Long clientId) {
        return ResponseEntity.ok(sessionService.getClientStats(clientId));
    }

    @Override
    public ResponseEntity<SessionResponse> getSession(Long id) {
        return ResponseEntity.ok(sessionService.getSession(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionResponse> updateSession(Long id, UpdateSessionRequest updateSessionRequest) {
        return ResponseEntity.ok(sessionService.updateSession(id, updateSessionRequest));
    }

    @Override
    public ResponseEntity<SessionResponse> cancelSession(Long id) {
        return ResponseEntity.ok(sessionService.cancelSession(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionResponse> completeSession(Long id, String notes) {
        return ResponseEntity.ok(sessionService.completeSession(id, notes));
    }

    @Override
    public ResponseEntity<SessionResponse> rescheduleSession(Long id, RescheduleSessionRequest rescheduleSessionRequest) {
        return ResponseEntity.ok(sessionService.rescheduleSession(id, rescheduleSessionRequest));
    }

    @Override
    public ResponseEntity<SessionResponse> acceptAgreement(Long id) {
        return ResponseEntity.ok(sessionService.acceptAgreement(id));
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
    }
}
