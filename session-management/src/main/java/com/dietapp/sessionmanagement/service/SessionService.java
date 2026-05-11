package com.dietapp.sessionmanagement.service;

import com.dietapp.api.sessionmanagement.model.*;
import com.dietapp.sessionmanagement.exception.PolicyViolationException;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.sessionmanagement.model.entity.RescheduleHistoryEntity;
import com.dietapp.sessionmanagement.model.entity.SessionEntity;
import com.dietapp.sessionmanagement.repository.RescheduleHistoryRepository;
import com.dietapp.sessionmanagement.repository.SessionRepository;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private final SessionRepository sessionRepository;
    private final RescheduleHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SessionPolicyService policyService;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SessionResponse> getAllSessions() {
        return sessionRepository.findAllByOrderBySessionDateDesc().stream()
                .map(s -> toResponse(s, historyRepository.findBySessionIdOrderByRescheduledAtDesc(s.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsByClientId(Long clientId) {
        return sessionRepository.findByClientIdOrderBySessionDateDesc(clientId).stream()
                .map(s -> toResponse(s, historyRepository.findBySessionIdOrderByRescheduledAtDesc(s.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(Long id) {
        SessionEntity session = findOrThrow(id);
        return toResponse(session, historyRepository.findBySessionIdOrderByRescheduledAtDesc(id));
    }

    @Transactional(readOnly = true)
    public ClientSessionStats getClientStats(Long clientId) {
        long total     = sessionRepository.countByClientId(clientId);
        long scheduled = sessionRepository.countByClientIdAndStatus(clientId, "scheduled");
        long completed = sessionRepository.countByClientIdAndStatus(clientId, "completed");
        long cancelled = sessionRepository.countByClientIdAndStatus(clientId, "cancelled");
        long rescheduled = sessionRepository.findByClientIdOrderBySessionDateDesc(clientId).stream()
                .filter(s -> historyRepository.countBySessionIdAndRescheduledAtAfter(s.getId(), Instant.EPOCH) > 0)
                .count();

        return new ClientSessionStats()
                .clientId(clientId)
                .total(total)
                .scheduled(scheduled)
                .completed(completed)
                .cancelled(cancelled)
                .rescheduled(rescheduled);
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    @Transactional
    public List<SessionResponse> createSession(CreateSessionRequest request) {
        int repeat = "weekly".equals(request.getRepeatType()) ? 4 : 1;
        List<SessionResponse> created = new ArrayList<>();

        for (int i = 0; i < repeat; i++) {
            LocalDate date = request.getSessionDate().plusWeeks(i);
            SessionEntity session = SessionEntity.builder()
                    .clientId(request.getClientId())
                    .type(request.getType().getValue())
                    .sessionDate(date)
                    .sessionTime(request.getSessionTime())
                    .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60)
                    .repeatType(request.getRepeatType() != null ? request.getRepeatType().getValue() : "none")
                    .meetLink(request.getMeetLink())
                    .notes(request.getNotes())
                    .status("scheduled")
                    .agreementAccepted(false)
                    .build();
            created.add(toResponse(sessionRepository.save(session), List.of()));
        }
        return created;
    }

    @Transactional
    public SessionResponse updateSession(Long id, UpdateSessionRequest request) {
        SessionEntity session = findOrThrow(id);

        if (request.getType() != null)            session.setType(request.getType().getValue());
        if (request.getSessionDate() != null)     session.setSessionDate(request.getSessionDate());
        if (request.getSessionTime() != null)     session.setSessionTime(request.getSessionTime());
        if (request.getDurationMinutes() != null) session.setDurationMinutes(request.getDurationMinutes());
        if (request.getRepeatType() != null)      session.setRepeatType(request.getRepeatType().getValue());
        if (request.getMeetLink() != null)        session.setMeetLink(request.getMeetLink());
        if (request.getNotes() != null)           session.setNotes(request.getNotes());

        session = sessionRepository.save(session);
        return toResponse(session, historyRepository.findBySessionIdOrderByRescheduledAtDesc(id));
    }

    @Transactional
    public SessionResponse cancelSession(Long id) {
        SessionEntity session = findOrThrow(id);
        session.setStatus("cancelled");
        return toResponse(sessionRepository.save(session),
                historyRepository.findBySessionIdOrderByRescheduledAtDesc(id));
    }

    @Transactional
    public SessionResponse completeSession(Long id, String notes) {
        SessionEntity session = findOrThrow(id);
        session.setStatus("completed");
        if (notes != null) session.setNotes(notes);
        return toResponse(sessionRepository.save(session),
                historyRepository.findBySessionIdOrderByRescheduledAtDesc(id));
    }

    @Transactional
    public SessionResponse rescheduleSession(Long id, RescheduleSessionRequest request) {
        SessionEntity session = findOrThrow(id);

        if (!"scheduled".equals(session.getStatus())) {
            throw new PolicyViolationException("Only scheduled sessions can be rescheduled.");
        }

        PolicyResponse policy = policyService.getEffectivePolicyForClient(session.getClientId());

        // Enforce notice period
        LocalDateTime sessionDT = LocalDateTime.of(session.getSessionDate(),
                LocalTime.parse(session.getSessionTime().toUpperCase(), TIME_FMT));
        long hoursUntil = Duration.between(LocalDateTime.now(), sessionDT).toHours();
        if (hoursUntil < policy.getCancellationNoticeHours()) {
            throw new PolicyViolationException(
                    "Minimum " + policy.getCancellationNoticeHours() + " hours notice required before rescheduling.");
        }

        // Enforce monthly reschedule limit
        Instant monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        long usedThisMonth = historyRepository.countBySessionIdAndRescheduledAtAfter(id, monthStart);
        if (usedThisMonth >= policy.getMaxReschedulePerMonth()) {
            throw new PolicyViolationException(
                    "Monthly reschedule limit of " + policy.getMaxReschedulePerMonth() + " has been reached.");
        }

        // Record history
        historyRepository.save(RescheduleHistoryEntity.builder()
                .sessionId(id)
                .fromDate(session.getSessionDate())
                .fromTime(session.getSessionTime())
                .toDate(request.getNewDate())
                .toTime(request.getNewTime())
                .rescheduledAt(Instant.now())
                .build());

        session.setSessionDate(request.getNewDate());
        session.setSessionTime(request.getNewTime());
        return toResponse(sessionRepository.save(session),
                historyRepository.findBySessionIdOrderByRescheduledAtDesc(id));
    }

    @Transactional
    public SessionResponse acceptAgreement(Long id) {
        SessionEntity session = findOrThrow(id);
        session.setAgreementAccepted(true);
        return toResponse(sessionRepository.save(session),
                historyRepository.findBySessionIdOrderByRescheduledAtDesc(id));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private SessionEntity findOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + id));
    }

    private String resolveClientName(Long clientId) {
        return userRepository.findById(clientId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");
    }

    private SessionResponse toResponse(SessionEntity s, List<RescheduleHistoryEntity> history) {
        List<RescheduleHistoryItem> histItems = history.stream()
                .map(h -> new RescheduleHistoryItem()
                        .id(h.getId())
                        .fromDate(h.getFromDate())
                        .fromTime(h.getFromTime())
                        .toDate(h.getToDate())
                        .toTime(h.getToTime())
                        .rescheduledAt(h.getRescheduledAt() != null
                                ? h.getRescheduledAt().atOffset(ZoneOffset.UTC) : null))
                .toList();

        return new SessionResponse()
                .id(s.getId())
                .clientId(s.getClientId())
                .clientName(resolveClientName(s.getClientId()))
                .type(SessionResponse.TypeEnum.fromValue(s.getType()))
                .sessionDate(s.getSessionDate())
                .sessionTime(s.getSessionTime())
                .durationMinutes(s.getDurationMinutes())
                .repeatType(SessionResponse.RepeatTypeEnum.fromValue(s.getRepeatType()))
                .meetLink(s.getMeetLink())
                .status(SessionResponse.StatusEnum.fromValue(s.getStatus()))
                .agreementAccepted(s.getAgreementAccepted())
                .notes(s.getNotes())
                .rescheduleHistory(histItems)
                .createdAt(s.getCreatedAt() != null
                        ? s.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }
}
