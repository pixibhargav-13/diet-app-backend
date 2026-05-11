package com.dietapp.consultationmanagement.controller;

import com.dietapp.api.consultation.api.AppointmentsApi;
import com.dietapp.api.consultation.api.PreConsultApi;
import com.dietapp.api.consultation.api.SessionSummariesApi;
import com.dietapp.api.consultation.model.*;
import com.dietapp.consultationmanagement.service.ConsultationService;
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
public class ConsultationController implements AppointmentsApi, PreConsultApi, SessionSummariesApi {

    private final ConsultationService consultationService;
    private final UserRepository userRepository;

    // ── Appointments ──────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(String status) {
        return ResponseEntity.ok(consultationService.getMyAppointments(currentUserId(), status));
    }

    @Override
    public ResponseEntity<AppointmentResponse> bookAppointment(BookAppointmentRequest bookAppointmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.bookAppointment(currentUserId(), bookAppointmentRequest));
    }

    @Override
    public ResponseEntity<AppointmentResponse> getAppointment(Long id) {
        return ResponseEntity.ok(consultationService.getAppointment(id));
    }

    @Override
    public ResponseEntity<AppointmentResponse> cancelAppointment(Long id) {
        return ResponseEntity.ok(consultationService.cancelAppointment(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(String status, Long clientId) {
        return ResponseEntity.ok(consultationService.getAllAppointments(status, clientId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentResponse> acceptAppointment(Long id, AcceptAppointmentRequest acceptAppointmentRequest) {
        return ResponseEntity.ok(consultationService.acceptAppointment(id, acceptAppointmentRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentResponse> completeAppointment(Long id) {
        return ResponseEntity.ok(consultationService.completeAppointment(id));
    }

    // ── Pre-Consult ───────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<PreConsultResponse> getPreConsult(Long id) {
        return consultationService.getPreConsult(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<PreConsultResponse> submitPreConsult(Long id, PreConsultRequest preConsultRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.submitPreConsult(id, preConsultRequest));
    }

    // ── Session Summaries ─────────────────────────────────────────────────────

    @Override
    public ResponseEntity<SessionSummaryResponse> getSessionSummary(Long id) {
        return consultationService.getSessionSummary(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionSummaryResponse> addSessionSummary(Long id, SessionSummaryRequest sessionSummaryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.addSessionSummary(id, sessionSummaryRequest));
    }

    @Override
    public ResponseEntity<SessionSummaryResponse> rateSession(Long id, RateSessionRequest rateSessionRequest) {
        return ResponseEntity.ok(consultationService.rateSession(id, rateSessionRequest));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
    }
}
