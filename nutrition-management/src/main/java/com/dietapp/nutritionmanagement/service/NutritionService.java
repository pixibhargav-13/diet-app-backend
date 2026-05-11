package com.dietapp.nutritionmanagement.service;

import com.dietapp.api.nutrition.model.*;
import com.dietapp.nutritionmanagement.model.entity.*;
import com.dietapp.nutritionmanagement.repository.*;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final MealLogRepository mealLogRepository;
    private final MealPlanRepository mealPlanRepository;
    private final ClientMealPlanAssignmentRepository mealPlanAssignmentRepository;
    private final FoodItemRepository foodItemRepository;

    // ── Meal Logs ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MealLogEntry> getMealLogs(Long userId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        return mealLogRepository.findByUserIdAndDateOrderByCreatedAtAsc(userId, target)
                .stream().map(this::toMealLogEntry).toList();
    }

    @Transactional
    public MealLogEntry logMeal(Long userId, LogMealRequest request) {
        MealLogEntity entity = MealLogEntity.builder()
                .userId(userId)
                .date(LocalDate.now())
                .mealType(request.getMealType().getValue())
                .mealName(request.getMealName())
                .kcal(BigDecimal.valueOf(request.getKcal()))
                .protein(request.getProtein() != null ? BigDecimal.valueOf(request.getProtein()) : BigDecimal.ZERO)
                .carbs(request.getCarbs() != null ? BigDecimal.valueOf(request.getCarbs()) : BigDecimal.ZERO)
                .fat(request.getFat() != null ? BigDecimal.valueOf(request.getFat()) : BigDecimal.ZERO)
                .fiber(request.getFiber() != null ? BigDecimal.valueOf(request.getFiber()) : BigDecimal.ZERO)
                .source(request.getSource() != null ? request.getSource().getValue() : "custom")
                .photoUrl(request.getPhotoUrl())
                .build();
        return toMealLogEntry(mealLogRepository.save(entity));
    }

    @Transactional
    public void deleteMealLog(Long id) {
        mealLogRepository.delete(mealLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal log not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<MealLogEntry> getClientMealLogs(Long clientId, LocalDate date) {
        return getMealLogs(clientId, date);
    }

    @Transactional(readOnly = true)
    public DaySummary getDaySummary(Long userId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        List<MealLogEntity> logs = mealLogRepository.findByUserIdAndDateOrderByCreatedAtAsc(userId, target);

        double totalKcal = logs.stream().mapToDouble(l -> l.getKcal().doubleValue()).sum();
        double totalProtein = logs.stream().mapToDouble(l -> l.getProtein().doubleValue()).sum();
        double totalCarbs = logs.stream().mapToDouble(l -> l.getCarbs().doubleValue()).sum();
        double totalFat = logs.stream().mapToDouble(l -> l.getFat().doubleValue()).sum();
        double totalFiber = logs.stream().mapToDouble(l -> l.getFiber().doubleValue()).sum();

        MacroTotals consumed = new MacroTotals()
                .kcal(totalKcal).protein(totalProtein).carbs(totalCarbs).fat(totalFat).fiber(totalFiber);

        MacroTotals goal = new MacroTotals().kcal(2000.0).protein(0.0).carbs(0.0).fat(0.0).fiber(0.0);

        return new DaySummary()
                .date(target)
                .consumed(consumed)
                .goal(goal)
                .mealsLogged(logs.size());
    }

    // ── Meal Plans ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MealPlan getMealPlanForDate(Long userId, LocalDate date) {
        return mealPlanAssignmentRepository.findTopByClientIdOrderByAssignedAtDesc(userId)
                .flatMap(a -> mealPlanRepository.findById(a.getMealPlanId()))
                .map(this::toMealPlan)
                .orElseThrow(() -> new ResourceNotFoundException("No meal plan assigned"));
    }

    @Transactional(readOnly = true)
    public List<MealPlan> getAllMealPlans() {
        return mealPlanRepository.findAll().stream().map(this::toMealPlan).toList();
    }

    @Transactional
    public MealPlan createMealPlan(MealPlanRequest request) {
        MealPlanEntity entity = new MealPlanEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        MealPlanEntity saved = mealPlanRepository.save(entity);
        if (request.getMeals() != null) {
            List<MealPlanItemEntity> items = request.getMeals().stream()
                    .map(item -> toItemEntity(item, saved))
                    .toList();
            saved.getMeals().addAll(items);
            mealPlanRepository.save(saved);
        }
        return toMealPlan(mealPlanRepository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public MealPlan updateMealPlan(Long id, MealPlanRequest request) {
        MealPlanEntity entity = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found: " + id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.getMeals().clear();
        if (request.getMeals() != null) {
            entity.getMeals().addAll(request.getMeals().stream()
                    .map(item -> toItemEntity(item, entity))
                    .toList());
        }
        return toMealPlan(mealPlanRepository.save(entity));
    }

    @Transactional
    public void deleteMealPlan(Long id) {
        mealPlanRepository.delete(mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found: " + id)));
    }

    @Transactional
    public void assignMealPlan(Long planId, AssignPlanRequest request) {
        mealPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found: " + planId));
        mealPlanAssignmentRepository.deleteByClientId(request.getClientId());
        mealPlanAssignmentRepository.save(ClientMealPlanAssignmentEntity.builder()
                .clientId(request.getClientId())
                .mealPlanId(planId)
                .build());
    }

    // ── Food Lookup ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FoodItem> searchFoods(String q, Integer limit) {
        int pageSize = limit != null ? limit : 20;
        return foodItemRepository.findByNameContainingIgnoreCase(q, PageRequest.of(0, pageSize))
                .stream().map(this::toFoodItem).toList();
    }

    @Transactional(readOnly = true)
    public FoodItem getFoodDetail(Long id) {
        return toFoodItem(foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found: " + id)));
    }

    // ── Grocery List ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GroceryList getGroceryList(Long userId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        Optional<MealPlanEntity> plan = mealPlanAssignmentRepository
                .findTopByClientIdOrderByAssignedAtDesc(userId)
                .flatMap(a -> mealPlanRepository.findById(a.getMealPlanId()));

        List<GroceryItem> items = plan.map(p -> p.getMeals().stream()
                .filter(item -> item.getIngredients() != null)
                .flatMap(item -> Arrays.stream(item.getIngredients().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(ingredient -> new GroceryItem()
                                .name(ingredient)
                                .category(GroceryItem.CategoryEnum.OTHER)
                                .quantity("1")
                                .unit("unit")))
                .toList()).orElse(List.of());

        return new GroceryList()
                .date(target)
                .items(items)
                .totalItems(items.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MealLogEntry toMealLogEntry(MealLogEntity e) {
        MealLogEntry entry = new MealLogEntry()
                .id(e.getId())
                .userId(e.getUserId())
                .date(e.getDate())
                .mealName(e.getMealName())
                .photoUrl(e.getPhotoUrl())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        if (e.getMealType() != null) {
            try { entry.setMealType(MealLogEntry.MealTypeEnum.fromValue(e.getMealType())); } catch (Exception ignored) {}
        }
        if (e.getSource() != null) {
            try { entry.setSource(MealLogEntry.SourceEnum.fromValue(e.getSource())); } catch (Exception ignored) {}
        }
        if (e.getKcal() != null) entry.setKcal(e.getKcal().doubleValue());
        if (e.getProtein() != null) entry.setProtein(e.getProtein().doubleValue());
        if (e.getCarbs() != null) entry.setCarbs(e.getCarbs().doubleValue());
        if (e.getFat() != null) entry.setFat(e.getFat().doubleValue());
        if (e.getFiber() != null) entry.setFiber(e.getFiber().doubleValue());
        return entry;
    }

    private MealPlan toMealPlan(MealPlanEntity e) {
        List<MealPlanItem> meals = e.getMeals().stream().map(this::toMealPlanItem).toList();
        return new MealPlan()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .meals(meals)
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private MealPlanItem toMealPlanItem(MealPlanItemEntity e) {
        MealPlanItem item = new MealPlanItem()
                .id(e.getId())
                .name(e.getName())
                .scheduledTime(e.getScheduledTime())
                .prepNotes(e.getPrepNotes());
        if (e.getMealType() != null) {
            try { item.setMealType(MealPlanItem.MealTypeEnum.fromValue(e.getMealType())); } catch (Exception ignored) {}
        }
        if (e.getIngredients() != null)
            item.setIngredients(Arrays.asList(e.getIngredients().split(",")));
        if (e.getKcal() != null) item.setKcal(e.getKcal().doubleValue());
        if (e.getProtein() != null) item.setProtein(e.getProtein().doubleValue());
        if (e.getCarbs() != null) item.setCarbs(e.getCarbs().doubleValue());
        if (e.getFat() != null) item.setFat(e.getFat().doubleValue());
        if (e.getFiber() != null) item.setFiber(e.getFiber().doubleValue());
        return item;
    }

    private MealPlanItemEntity toItemEntity(MealPlanItem item, MealPlanEntity plan) {
        MealPlanItemEntity entity = MealPlanItemEntity.builder()
                .mealPlan(plan)
                .name(item.getName())
                .mealType(item.getMealType() != null ? item.getMealType().getValue() : null)
                .scheduledTime(item.getScheduledTime())
                .prepNotes(item.getPrepNotes())
                .ingredients(item.getIngredients() != null ? String.join(",", item.getIngredients()) : null)
                .build();
        if (item.getKcal() != null) entity.setKcal(BigDecimal.valueOf(item.getKcal()));
        if (item.getProtein() != null) entity.setProtein(BigDecimal.valueOf(item.getProtein()));
        if (item.getCarbs() != null) entity.setCarbs(BigDecimal.valueOf(item.getCarbs()));
        if (item.getFat() != null) entity.setFat(BigDecimal.valueOf(item.getFat()));
        if (item.getFiber() != null) entity.setFiber(BigDecimal.valueOf(item.getFiber()));
        return entity;
    }

    private FoodItem toFoodItem(FoodItemEntity e) {
        FoodItem item = new FoodItem()
                .id(e.getId())
                .name(e.getName())
                .source(e.getSource());
        if (e.getServingSizeG() != null) item.setServingSizeG(e.getServingSizeG().doubleValue());
        if (e.getKcalPer100g() != null) item.setKcalPer100g(e.getKcalPer100g().doubleValue());
        if (e.getProteinPer100g() != null) item.setProteinPer100g(e.getProteinPer100g().doubleValue());
        if (e.getCarbsPer100g() != null) item.setCarbsPer100g(e.getCarbsPer100g().doubleValue());
        if (e.getFatPer100g() != null) item.setFatPer100g(e.getFatPer100g().doubleValue());
        if (e.getFiberPer100g() != null) item.setFiberPer100g(e.getFiberPer100g().doubleValue());
        return item;
    }
}
