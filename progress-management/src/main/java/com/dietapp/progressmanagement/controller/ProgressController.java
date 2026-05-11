package com.dietapp.progressmanagement.controller;

import com.dietapp.api.progress.api.HydrationApi;
import com.dietapp.api.progress.api.MeasurementsApi;
import com.dietapp.api.progress.api.StepsApi;
import com.dietapp.api.progress.api.WeightTrackingApi;
import com.dietapp.api.progress.model.*;
import com.dietapp.progressmanagement.service.ProgressService;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProgressController implements WeightTrackingApi, MeasurementsApi, StepsApi, HydrationApi {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    // ── Weight ────────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<WeightEntry>> getWeightHistory(LocalDate from, LocalDate to) {
        return ResponseEntity.ok(progressService.getWeightHistory(currentUserId(), from, to));
    }

    @Override
    public ResponseEntity<WeightEntry> logWeight(WeightRequest weightRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.logWeight(currentUserId(), weightRequest));
    }

    @Override
    public ResponseEntity<Void> deleteWeightEntry(Long id) {
        progressService.deleteWeightEntry(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<WeightGoal> getWeightGoal() {
        return ResponseEntity.ok(progressService.getWeightGoal(currentUserId()));
    }

    @Override
    public ResponseEntity<WeightGoal> updateWeightGoal(WeightGoal weightGoal) {
        return ResponseEntity.ok(progressService.updateWeightGoal(currentUserId(), weightGoal));
    }

    // ── Measurements ──────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<MeasurementEntry>> getMeasurements(LocalDate from, LocalDate to) {
        return ResponseEntity.ok(progressService.getMeasurements(currentUserId(), from, to));
    }

    @Override
    public ResponseEntity<MeasurementEntry> logMeasurement(MeasurementRequest measurementRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.logMeasurement(currentUserId(), measurementRequest));
    }

    @Override
    public ResponseEntity<Void> deleteMeasurement(Long id) {
        progressService.deleteMeasurement(id);
        return ResponseEntity.noContent().build();
    }

    // ── Steps ─────────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<StepEntry>> getStepHistory(LocalDate from, LocalDate to) {
        return ResponseEntity.ok(progressService.getStepHistory(currentUserId(), from, to));
    }

    @Override
    public ResponseEntity<StepEntry> logSteps(StepRequest stepRequest) {
        return ResponseEntity.ok(progressService.logSteps(currentUserId(), stepRequest));
    }

    @Override
    public ResponseEntity<StepSummary> getStepSummary() {
        return ResponseEntity.ok(progressService.getStepSummary(currentUserId()));
    }

    // ── Hydration ─────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<HydrationEntry>> getHydrationEntries(LocalDate date) {
        return ResponseEntity.ok(progressService.getHydrationEntries(currentUserId(), date));
    }

    @Override
    public ResponseEntity<HydrationEntry> logHydration(HydrationRequest hydrationRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.logHydration(currentUserId(), hydrationRequest));
    }

    @Override
    public ResponseEntity<Void> deleteHydrationEntry(Long id) {
        progressService.deleteHydrationEntry(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<HydrationGoal> getHydrationGoal() {
        return ResponseEntity.ok(progressService.getHydrationGoal(currentUserId()));
    }

    @Override
    public ResponseEntity<HydrationGoal> updateHydrationGoal(HydrationGoal hydrationGoal) {
        return ResponseEntity.ok(progressService.updateHydrationGoal(currentUserId(), hydrationGoal));
    }

    @Override
    public ResponseEntity<HydrationSummary> getHydrationSummary() {
        return ResponseEntity.ok(progressService.getHydrationSummary(currentUserId()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
    }
}
