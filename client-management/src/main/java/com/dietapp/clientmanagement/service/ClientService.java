package com.dietapp.clientmanagement.service;

import com.dietapp.api.clientmgmt.model.*;
import com.dietapp.clientmanagement.model.entity.*;
import com.dietapp.clientmanagement.repository.*;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.sessionmanagement.repository.SessionRepository;
import com.dietapp.usermanagement.model.entity.UserEntity;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final UserRepository userRepository;
    private final OnboardingRepository onboardingRepository;
    private final ClientDietPlanAssignmentRepository dietPlanAssignmentRepository;
    private final ClientNoteRepository noteRepository;
    private final MealFeedbackRepository feedbackRepository;
    private final SessionRepository sessionRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<ClientSummary> getAllClients(String search, String goal, String status, String sortBy) {
        List<UserEntity> users = userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .toList();

        return users.stream()
                .filter(u -> search == null || u.getFirstName().toLowerCase().contains(search.toLowerCase())
                        || u.getLastName().toLowerCase().contains(search.toLowerCase())
                        || u.getEmail().toLowerCase().contains(search.toLowerCase()))
                .filter(u -> {
                    if (goal == null) return true;
                    Optional<OnboardingEntity> ob = onboardingRepository.findByUserId(u.getId());
                    return ob.map(o -> goal.equalsIgnoreCase(o.getGoal())).orElse(false);
                })
                .filter(u -> {
                    if (status == null) return true;
                    return status.equalsIgnoreCase(u.getEnabled() ? "active" : "inactive");
                })
                .map(u -> buildClientSummary(u))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientProfile getClientProfile(Long clientId) {
        UserEntity user = findUserOrThrow(clientId);
        return buildClientProfile(user);
    }

    @Transactional
    public ClientProfile updateClientProfile(Long clientId, UpdateClientRequest request) {
        UserEntity user = findUserOrThrow(clientId);

        if (request.getStatus() != null) {
            user.setEnabled("active".equalsIgnoreCase(request.getStatus().getValue()));
            userRepository.save(user);
        }

        if (request.getGoal() != null) {
            OnboardingEntity ob = onboardingRepository.findByUserId(clientId)
                    .orElseGet(() -> OnboardingEntity.builder().userId(clientId).build());
            ob.setGoal(request.getGoal());
            onboardingRepository.save(ob);
        }

        return buildClientProfile(userRepository.findById(clientId).orElseThrow());
    }

    @Transactional
    public ClientProfile updateCompliance(Long clientId, ComplianceUpdateRequest request) {
        findUserOrThrow(clientId);
        return buildClientProfile(userRepository.findById(clientId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<ClientNote> getClientNotes(Long clientId) {
        findUserOrThrow(clientId);
        return noteRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toClientNote)
                .toList();
    }

    @Transactional
    public ClientNote addClientNote(Long clientId, ClientNoteRequest request) {
        findUserOrThrow(clientId);
        ClientNoteEntity entity = ClientNoteEntity.builder()
                .clientId(clientId)
                .note(request.getNote())
                .build();
        return toClientNote(noteRepository.save(entity));
    }

    @Transactional
    public MealFeedback submitMealFeedback(Long clientId, MealFeedbackRequest request) {
        findUserOrThrow(clientId);
        MealFeedbackEntity entity = MealFeedbackEntity.builder()
                .clientId(clientId)
                .mealLogId(request.getMealLogId())
                .feedback(request.getFeedback())
                .rating(request.getRating())
                .build();
        return toMealFeedback(feedbackRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public AdminDashboardStats getAdminDashboardStats() {
        long totalClients = userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole())).count();
        long activeToday = userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole()) && u.getEnabled()).count();
        long sessionsThisWeek = sessionRepository.findAllByOrderBySessionDateDesc().stream()
                .filter(s -> s.getSessionDate() != null
                        && s.getSessionDate().isAfter(LocalDate.now().minusDays(7)))
                .count();
        double monthlyRevenue = transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(t -> t.getCreatedAt() != null
                        && t.getCreatedAt().isAfter(Instant.now().minusSeconds(30L * 24 * 3600)))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
        long inactiveCount = userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole()) && !u.getEnabled()).count();

        return new AdminDashboardStats()
                .totalClients((int) totalClients)
                .activeToday((int) activeToday)
                .sessionsThisWeek((int) sessionsThisWeek)
                .monthlyRevenue(monthlyRevenue)
                .inactiveClientsCount((int) inactiveCount);
    }

    @Transactional(readOnly = true)
    public List<ActivityItem> getActivityFeed(LocalDate date) {
        return List.of();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserEntity findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
    }

    private ClientSummary buildClientSummary(UserEntity user) {
        Optional<OnboardingEntity> ob = onboardingRepository.findByUserId(user.getId());
        Optional<ClientDietPlanAssignmentEntity> plan = dietPlanAssignmentRepository
                .findTopByClientIdOrderByAssignedAtDesc(user.getId());

        return new ClientSummary()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .goal(ob.map(OnboardingEntity::getGoal).orElse(null))
                .status(user.getEnabled()
                        ? ClientSummary.StatusEnum.ACTIVE
                        : ClientSummary.StatusEnum.INACTIVE)
                .lastActive(user.getUpdatedAt() != null
                        ? user.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private ClientProfile buildClientProfile(UserEntity user) {
        Optional<OnboardingEntity> ob = onboardingRepository.findByUserId(user.getId());

        ClientProfile profile = new ClientProfile()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .status(user.getEnabled()
                        ? ClientProfile.StatusEnum.ACTIVE
                        : ClientProfile.StatusEnum.INACTIVE)
                .onboardingComplete(ob.map(OnboardingEntity::getComplete).orElse(false))
                .createdAt(user.getCreatedAt() != null
                        ? user.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .lastActive(user.getUpdatedAt() != null
                        ? user.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);

        ob.ifPresent(o -> profile
                .goal(o.getGoal())
                .allergies(o.getAllergies())
                .dietaryPreference(o.getDietaryPreference()));

        return profile;
    }

    private ClientNote toClientNote(ClientNoteEntity e) {
        return new ClientNote()
                .id(e.getId())
                .clientId(e.getClientId())
                .note(e.getNote())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private MealFeedback toMealFeedback(MealFeedbackEntity e) {
        return new MealFeedback()
                .id(e.getId())
                .clientId(e.getClientId())
                .mealLogId(e.getMealLogId())
                .feedback(e.getFeedback())
                .rating(e.getRating())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }
}
