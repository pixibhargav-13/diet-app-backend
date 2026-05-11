package com.dietapp.clientmanagement.controller;

import com.dietapp.api.clientmgmt.api.DietPlansApi;
import com.dietapp.api.clientmgmt.model.AssignPlanRequest;
import com.dietapp.api.clientmgmt.model.DietPlanRequest;
import com.dietapp.api.clientmgmt.model.DietPlanResponse;
import com.dietapp.clientmanagement.service.DietPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DietPlanController implements DietPlansApi {

    private final DietPlanService dietPlanService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DietPlanResponse>> getAllDietPlans() {
        return ResponseEntity.ok(dietPlanService.getAllDietPlans());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DietPlanResponse> createDietPlan(DietPlanRequest dietPlanRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dietPlanService.createDietPlan(dietPlanRequest));
    }

    @Override
    public ResponseEntity<DietPlanResponse> getDietPlan(Long id) {
        return ResponseEntity.ok(dietPlanService.getDietPlan(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DietPlanResponse> updateDietPlan(Long id, DietPlanRequest dietPlanRequest) {
        return ResponseEntity.ok(dietPlanService.updateDietPlan(id, dietPlanRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDietPlan(Long id) {
        dietPlanService.deleteDietPlan(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignDietPlan(Long id, AssignPlanRequest assignPlanRequest) {
        dietPlanService.assignDietPlan(id, assignPlanRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<DietPlanResponse> getClientCurrentPlan(Long clientId) {
        return dietPlanService.getClientCurrentPlan(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
