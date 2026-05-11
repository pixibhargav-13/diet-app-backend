package com.dietapp.nutritionmanagement.controller;

import com.dietapp.api.nutrition.api.FoodLookupApi;
import com.dietapp.api.nutrition.api.GroceryListApi;
import com.dietapp.api.nutrition.api.MealLogsApi;
import com.dietapp.api.nutrition.api.MealPlansApi;
import com.dietapp.api.nutrition.model.*;
import com.dietapp.nutritionmanagement.service.NutritionService;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.context.request.NativeWebRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class NutritionController implements MealLogsApi, MealPlansApi, FoodLookupApi, GroceryListApi {

    private final NutritionService nutritionService;
    private final UserRepository userRepository;

    // ── Meal Logs ─────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<MealLogEntry>> getMealLogs(LocalDate date) {
        return ResponseEntity.ok(nutritionService.getMealLogs(currentUserId(), date));
    }

    @Override
    public ResponseEntity<MealLogEntry> logMeal(LogMealRequest logMealRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nutritionService.logMeal(currentUserId(), logMealRequest));
    }

    @Override
    public ResponseEntity<Void> deleteMealLog(Long id) {
        nutritionService.deleteMealLog(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MealLogEntry>> getClientMealLogs(Long clientId, LocalDate date) {
        return ResponseEntity.ok(nutritionService.getClientMealLogs(clientId, date));
    }

    @Override
    public ResponseEntity<DaySummary> getDaySummary(LocalDate date) {
        return ResponseEntity.ok(nutritionService.getDaySummary(currentUserId(), date));
    }

    // ── Meal Plans ────────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<MealPlan> getMealPlanForDate(LocalDate date) {
        return ResponseEntity.ok(nutritionService.getMealPlanForDate(currentUserId(), date));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MealPlan>> getAllMealPlans() {
        return ResponseEntity.ok(nutritionService.getAllMealPlans());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MealPlan> createMealPlan(MealPlanRequest mealPlanRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nutritionService.createMealPlan(mealPlanRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MealPlan> updateMealPlan(Long id, MealPlanRequest mealPlanRequest) {
        return ResponseEntity.ok(nutritionService.updateMealPlan(id, mealPlanRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMealPlan(Long id) {
        nutritionService.deleteMealPlan(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignMealPlan(Long id, AssignPlanRequest assignPlanRequest) {
        nutritionService.assignMealPlan(id, assignPlanRequest);
        return ResponseEntity.ok().build();
    }

    // ── Food Lookup ───────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<List<FoodItem>> searchFoods(String q, Integer limit) {
        return ResponseEntity.ok(nutritionService.searchFoods(q, limit));
    }

    @Override
    public ResponseEntity<FoodItem> getFoodDetail(Long id) {
        return ResponseEntity.ok(nutritionService.getFoodDetail(id));
    }

    // ── Grocery List ──────────────────────────────────────────────────────────

    @Override
    public ResponseEntity<GroceryList> getGroceryList(LocalDate date) {
        return ResponseEntity.ok(nutritionService.getGroceryList(currentUserId(), date));
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
    }
}
