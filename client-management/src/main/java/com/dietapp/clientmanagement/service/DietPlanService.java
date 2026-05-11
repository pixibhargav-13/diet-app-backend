package com.dietapp.clientmanagement.service;

import com.dietapp.api.clientmgmt.model.AssignPlanRequest;
import com.dietapp.api.clientmgmt.model.DietPlanRequest;
import com.dietapp.api.clientmgmt.model.DietPlanResponse;
import com.dietapp.clientmanagement.model.entity.ClientDietPlanAssignmentEntity;
import com.dietapp.clientmanagement.model.entity.DietPlanEntity;
import com.dietapp.clientmanagement.repository.ClientDietPlanAssignmentRepository;
import com.dietapp.clientmanagement.repository.DietPlanRepository;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;
    private final ClientDietPlanAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<DietPlanResponse> getAllDietPlans() {
        return dietPlanRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public DietPlanResponse createDietPlan(DietPlanRequest request) {
        DietPlanEntity entity = buildEntity(new DietPlanEntity(), request);
        return toResponse(dietPlanRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public DietPlanResponse getDietPlan(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public DietPlanResponse updateDietPlan(Long id, DietPlanRequest request) {
        DietPlanEntity entity = buildEntity(findOrThrow(id), request);
        return toResponse(dietPlanRepository.save(entity));
    }

    @Transactional
    public void deleteDietPlan(Long id) {
        dietPlanRepository.delete(findOrThrow(id));
    }

    @Transactional
    public void assignDietPlan(Long planId, AssignPlanRequest request) {
        findOrThrow(planId);
        assignmentRepository.deleteByClientId(request.getClientId());
        assignmentRepository.save(ClientDietPlanAssignmentEntity.builder()
                .clientId(request.getClientId())
                .dietPlanId(planId)
                .build());
    }

    @Transactional(readOnly = true)
    public Optional<DietPlanResponse> getClientCurrentPlan(Long clientId) {
        return assignmentRepository.findTopByClientIdOrderByAssignedAtDesc(clientId)
                .flatMap(a -> dietPlanRepository.findById(a.getDietPlanId()))
                .map(this::toResponse);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private DietPlanEntity findOrThrow(Long id) {
        return dietPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diet plan not found: " + id));
    }

    private DietPlanEntity buildEntity(DietPlanEntity entity, DietPlanRequest req) {
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setTargetCalories(req.getTargetCalories());
        if (req.getTargetProteinG() != null) entity.setTargetProteinG(req.getTargetProteinG());
        if (req.getTargetCarbsG()  != null) entity.setTargetCarbsG(req.getTargetCarbsG());
        if (req.getTargetFatG()    != null) entity.setTargetFatG(req.getTargetFatG());
        return entity;
    }

    private DietPlanResponse toResponse(DietPlanEntity e) {
        long assigned = assignmentRepository.countByDietPlanId(e.getId());
        DietPlanResponse r = new DietPlanResponse()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .targetCalories(e.getTargetCalories())
                .assignedClientCount((int) assigned)
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);

        if (e.getTargetProteinG() != null) r.setTargetProteinG(e.getTargetProteinG());
        if (e.getTargetCarbsG()  != null) r.setTargetCarbsG(e.getTargetCarbsG());
        if (e.getTargetFatG()    != null) r.setTargetFatG(e.getTargetFatG());
        return r;
    }
}
