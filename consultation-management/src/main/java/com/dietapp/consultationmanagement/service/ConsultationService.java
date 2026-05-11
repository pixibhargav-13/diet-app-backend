package com.dietapp.consultationmanagement.service;

import com.dietapp.api.consultation.model.*;
import com.dietapp.consultationmanagement.model.entity.AppointmentEntity;
import com.dietapp.consultationmanagement.model.entity.PreConsultEntity;
import com.dietapp.consultationmanagement.model.entity.SessionSummaryEntity;
import com.dietapp.consultationmanagement.repository.AppointmentRepository;
import com.dietapp.consultationmanagement.repository.PreConsultRepository;
import com.dietapp.consultationmanagement.repository.SessionSummaryRepository;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final AppointmentRepository appointmentRepository;
    private final PreConsultRepository preConsultRepository;
    private final SessionSummaryRepository sessionSummaryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long userId, String status) {
        List<AppointmentEntity> entities = status != null
                ? appointmentRepository.findByClientIdAndStatusOrderByAppointmentDateDesc(userId, status)
                : appointmentRepository.findByClientIdOrderByAppointmentDateDesc(userId);
        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AppointmentResponse bookAppointment(Long userId, BookAppointmentRequest request) {
        AppointmentEntity entity = AppointmentEntity.builder()
                .clientId(userId)
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .type(request.getType().getValue())
                .status("pending")
                .notes(request.getNotes())
                .build();
        return toResponse(appointmentRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id) {
        AppointmentEntity entity = findOrThrow(id);
        entity.setStatus("cancelled");
        return toResponse(appointmentRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(String status, Long clientId) {
        List<AppointmentEntity> entities;
        if (clientId != null && status != null) {
            entities = appointmentRepository
                    .findByClientIdAndStatusOrderByAppointmentDateDesc(clientId, status);
        } else if (clientId != null) {
            entities = appointmentRepository
                    .findByClientIdOrderByAppointmentDateDesc(clientId);
        } else if (status != null) {
            entities = appointmentRepository.findByStatusOrderByAppointmentDateDesc(status);
        } else {
            entities = appointmentRepository.findAllByOrderByAppointmentDateDesc();
        }
        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AppointmentResponse acceptAppointment(Long id, AcceptAppointmentRequest request) {
        AppointmentEntity entity = findOrThrow(id);
        entity.setStatus("confirmed");
        entity.setMeetLink(request.getMeetLink());
        if (request.getConfirmedDate() != null) entity.setAppointmentDate(request.getConfirmedDate());
        if (request.getConfirmedTime() != null) entity.setAppointmentTime(request.getConfirmedTime());
        return toResponse(appointmentRepository.save(entity));
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long id) {
        AppointmentEntity entity = findOrThrow(id);
        entity.setStatus("completed");
        return toResponse(appointmentRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Optional<PreConsultResponse> getPreConsult(Long appointmentId) {
        return preConsultRepository.findByAppointmentId(appointmentId).map(this::toPreConsultResponse);
    }

    @Transactional
    public PreConsultResponse submitPreConsult(Long appointmentId, PreConsultRequest request) {
        AppointmentEntity appointment = findOrThrow(appointmentId);
        PreConsultEntity entity = preConsultRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> PreConsultEntity.builder().appointmentId(appointmentId).build());

        entity.setCurrentConcerns(request.getCurrentConcerns());
        entity.setRecentDietChanges(request.getRecentDietChanges());
        entity.setCurrentMedications(request.getCurrentMedications());
        if (request.getLabReportUrls() != null)
            entity.setLabReportUrls(String.join(",", request.getLabReportUrls()));
        entity.setAdditionalNotes(request.getAdditionalNotes());

        appointment.setPreConsultSubmitted(true);
        appointmentRepository.save(appointment);

        return toPreConsultResponse(preConsultRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Optional<SessionSummaryResponse> getSessionSummary(Long appointmentId) {
        return sessionSummaryRepository.findByAppointmentId(appointmentId).map(this::toSummaryResponse);
    }

    @Transactional
    public SessionSummaryResponse addSessionSummary(Long appointmentId, SessionSummaryRequest request) {
        findOrThrow(appointmentId);
        SessionSummaryEntity entity = sessionSummaryRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> SessionSummaryEntity.builder().appointmentId(appointmentId).build());

        entity.setNotes(request.getNotes());
        entity.setRecommendations(request.getRecommendations());
        if (request.getFollowUpRequired() != null) entity.setFollowUpRequired(request.getFollowUpRequired());

        return toSummaryResponse(sessionSummaryRepository.save(entity));
    }

    @Transactional
    public SessionSummaryResponse rateSession(Long appointmentId, RateSessionRequest request) {
        findOrThrow(appointmentId);
        SessionSummaryEntity entity = sessionSummaryRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session summary not found for appointment: " + appointmentId));

        entity.setClientRating(request.getRating());
        entity.setClientFeedback(request.getFeedback());

        AppointmentEntity appointment = findOrThrow(appointmentId);
        appointment.setRating(request.getRating());
        appointmentRepository.save(appointment);

        return toSummaryResponse(sessionSummaryRepository.save(entity));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AppointmentEntity findOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    private AppointmentResponse toResponse(AppointmentEntity e) {
        String clientName = userRepository.findById(e.getClientId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");

        AppointmentResponse r = new AppointmentResponse()
                .id(e.getId())
                .clientId(e.getClientId())
                .clientName(clientName)
                .appointmentDate(e.getAppointmentDate())
                .appointmentTime(e.getAppointmentTime())
                .meetLink(e.getMeetLink())
                .preConsultSubmitted(e.getPreConsultSubmitted())
                .rating(e.getRating())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);

        if (e.getType() != null) {
            try { r.setType(AppointmentResponse.TypeEnum.fromValue(e.getType())); } catch (Exception ignored) {}
        }
        if (e.getStatus() != null) {
            try { r.setStatus(AppointmentResponse.StatusEnum.fromValue(e.getStatus())); } catch (Exception ignored) {}
        }
        return r;
    }

    private PreConsultResponse toPreConsultResponse(PreConsultEntity e) {
        List<String> urls = e.getLabReportUrls() != null && !e.getLabReportUrls().isBlank()
                ? Arrays.asList(e.getLabReportUrls().split(",")) : List.of();
        return new PreConsultResponse()
                .id(e.getId())
                .appointmentId(e.getAppointmentId())
                .currentConcerns(e.getCurrentConcerns())
                .recentDietChanges(e.getRecentDietChanges())
                .currentMedications(e.getCurrentMedications())
                .labReportUrls(urls)
                .additionalNotes(e.getAdditionalNotes())
                .submittedAt(e.getSubmittedAt() != null ? e.getSubmittedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private SessionSummaryResponse toSummaryResponse(SessionSummaryEntity e) {
        return new SessionSummaryResponse()
                .id(e.getId())
                .appointmentId(e.getAppointmentId())
                .notes(e.getNotes())
                .recommendations(e.getRecommendations())
                .followUpRequired(e.getFollowUpRequired())
                .clientRating(e.getClientRating())
                .clientFeedback(e.getClientFeedback())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }
}
