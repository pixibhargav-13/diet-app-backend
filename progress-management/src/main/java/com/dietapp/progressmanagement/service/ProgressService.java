package com.dietapp.progressmanagement.service;

import com.dietapp.api.progress.model.*;
import com.dietapp.progressmanagement.model.entity.*;
import com.dietapp.progressmanagement.repository.*;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final WeightEntryRepository weightEntryRepository;
    private final WeightGoalRepository weightGoalRepository;
    private final MeasurementEntryRepository measurementEntryRepository;
    private final StepEntryRepository stepEntryRepository;
    private final HydrationEntryRepository hydrationEntryRepository;
    private final HydrationGoalRepository hydrationGoalRepository;

    // ── Weight ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WeightEntry> getWeightHistory(Long userId, LocalDate from, LocalDate to) {
        List<WeightEntryEntity> entries = (from != null && to != null)
                ? weightEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)
                : weightEntryRepository.findByUserIdOrderByDateAsc(userId);
        return entries.stream().map(this::toWeightEntry).toList();
    }

    @Transactional
    public WeightEntry logWeight(Long userId, WeightRequest request) {
        WeightEntryEntity entity = WeightEntryEntity.builder()
                .userId(userId)
                .weightKg(BigDecimal.valueOf(request.getWeightKg()))
                .date(request.getDate())
                .notes(request.getNotes())
                .build();
        return toWeightEntry(weightEntryRepository.save(entity));
    }

    @Transactional
    public void deleteWeightEntry(Long id) {
        weightEntryRepository.delete(weightEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Weight entry not found: " + id)));
    }

    @Transactional(readOnly = true)
    public WeightGoal getWeightGoal(Long userId) {
        return weightGoalRepository.findByUserId(userId)
                .map(e -> new WeightGoal().goalWeightKg(e.getGoalWeightKg().doubleValue()))
                .orElse(new WeightGoal());
    }

    @Transactional
    public WeightGoal updateWeightGoal(Long userId, WeightGoal request) {
        WeightGoalEntity entity = weightGoalRepository.findByUserId(userId)
                .orElseGet(() -> WeightGoalEntity.builder().userId(userId).goalWeightKg(BigDecimal.ZERO).build());
        if (request.getGoalWeightKg() != null) {
            entity.setGoalWeightKg(BigDecimal.valueOf(request.getGoalWeightKg()));
        }
        weightGoalRepository.save(entity);
        return new WeightGoal().goalWeightKg(entity.getGoalWeightKg().doubleValue());
    }

    // ── Measurements ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MeasurementEntry> getMeasurements(Long userId, LocalDate from, LocalDate to) {
        List<MeasurementEntryEntity> entries = (from != null && to != null)
                ? measurementEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)
                : measurementEntryRepository.findByUserIdOrderByDateAsc(userId);
        return entries.stream().map(this::toMeasurementEntry).toList();
    }

    @Transactional
    public MeasurementEntry logMeasurement(Long userId, MeasurementRequest request) {
        MeasurementEntryEntity entity = MeasurementEntryEntity.builder()
                .userId(userId)
                .date(request.getDate())
                .waistCm(request.getWaistCm() != null ? BigDecimal.valueOf(request.getWaistCm()) : null)
                .hipCm(request.getHipCm() != null ? BigDecimal.valueOf(request.getHipCm()) : null)
                .chestCm(request.getChestCm() != null ? BigDecimal.valueOf(request.getChestCm()) : null)
                .armCm(request.getArmCm() != null ? BigDecimal.valueOf(request.getArmCm()) : null)
                .thighCm(request.getThighCm() != null ? BigDecimal.valueOf(request.getThighCm()) : null)
                .build();
        return toMeasurementEntry(measurementEntryRepository.save(entity));
    }

    @Transactional
    public void deleteMeasurement(Long id) {
        measurementEntryRepository.delete(measurementEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement entry not found: " + id)));
    }

    // ── Steps ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StepEntry> getStepHistory(Long userId, LocalDate from, LocalDate to) {
        List<StepEntryEntity> entries = (from != null && to != null)
                ? stepEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)
                : stepEntryRepository.findByUserIdOrderByDateAsc(userId);
        return entries.stream().map(this::toStepEntry).toList();
    }

    @Transactional
    public StepEntry logSteps(Long userId, StepRequest request) {
        StepEntryEntity entity = stepEntryRepository.findByUserIdAndDate(userId, request.getDate())
                .orElseGet(() -> StepEntryEntity.builder().userId(userId).date(request.getDate()).steps(0).build());
        entity.setSteps(request.getSteps());
        return toStepEntry(stepEntryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public StepSummary getStepSummary(Long userId) {
        LocalDate today = LocalDate.now();
        int todaySteps = stepEntryRepository.findByUserIdAndDate(userId, today)
                .map(StepEntryEntity::getSteps).orElse(0);

        LocalDate weekStart = today.minusDays(6);
        List<StepEntryEntity> weekEntries = stepEntryRepository
                .findByUserIdAndDateBetweenOrderByDateAsc(userId, weekStart, today);
        int weeklyTotal = weekEntries.stream().mapToInt(StepEntryEntity::getSteps).sum();
        int weeklyAvg = weekEntries.isEmpty() ? 0 : weeklyTotal / 7;

        return new StepSummary()
                .todaySteps(todaySteps)
                .weeklyAvg(weeklyAvg)
                .weeklyTotal(weeklyTotal);
    }

    // ── Hydration ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HydrationEntry> getHydrationEntries(Long userId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        return hydrationEntryRepository.findByUserIdAndDateOrderByLoggedAtAsc(userId, target)
                .stream().map(this::toHydrationEntry).toList();
    }

    @Transactional
    public HydrationEntry logHydration(Long userId, HydrationRequest request) {
        HydrationEntryEntity entity = HydrationEntryEntity.builder()
                .userId(userId)
                .date(LocalDate.now())
                .ml(request.getMl())
                .source(request.getSource() != null ? request.getSource() : "manual")
                .build();
        return toHydrationEntry(hydrationEntryRepository.save(entity));
    }

    @Transactional
    public void deleteHydrationEntry(Long id) {
        hydrationEntryRepository.delete(hydrationEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hydration entry not found: " + id)));
    }

    @Transactional(readOnly = true)
    public HydrationGoal getHydrationGoal(Long userId) {
        return hydrationGoalRepository.findByUserId(userId)
                .map(e -> new HydrationGoal().goalMl(e.getGoalMl()))
                .orElse(new HydrationGoal().goalMl(2000));
    }

    @Transactional
    public HydrationGoal updateHydrationGoal(Long userId, HydrationGoal request) {
        HydrationGoalEntity entity = hydrationGoalRepository.findByUserId(userId)
                .orElseGet(() -> HydrationGoalEntity.builder().userId(userId).goalMl(2000).build());
        if (request.getGoalMl() != null) entity.setGoalMl(request.getGoalMl());
        hydrationGoalRepository.save(entity);
        return new HydrationGoal().goalMl(entity.getGoalMl());
    }

    @Transactional(readOnly = true)
    public HydrationSummary getHydrationSummary(Long userId) {
        LocalDate today = LocalDate.now();
        int goalMl = hydrationGoalRepository.findByUserId(userId)
                .map(HydrationGoalEntity::getGoalMl).orElse(2000);

        int totalMl = hydrationEntryRepository.findByUserIdAndDateOrderByLoggedAtAsc(userId, today)
                .stream().mapToInt(HydrationEntryEntity::getMl).sum();

        double pct = goalMl > 0 ? (totalMl * 100.0 / goalMl) : 0.0;

        int streak = computeStreak(userId, today);

        return new HydrationSummary()
                .date(today)
                .totalMl(totalMl)
                .goalMl(goalMl)
                .percentageComplete(pct)
                .weeklyStreakDays(streak)
                .consistencyPercent(streak / 7.0 * 100);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int computeStreak(Long userId, LocalDate today) {
        int streak = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.minusDays(i);
            boolean hasEntry = !hydrationEntryRepository.findByUserIdAndDateOrderByLoggedAtAsc(userId, day).isEmpty();
            if (hasEntry) streak++;
            else break;
        }
        return streak;
    }

    private WeightEntry toWeightEntry(WeightEntryEntity e) {
        return new WeightEntry()
                .id(e.getId())
                .userId(e.getUserId())
                .weightKg(e.getWeightKg().doubleValue())
                .date(e.getDate())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private MeasurementEntry toMeasurementEntry(MeasurementEntryEntity e) {
        MeasurementEntry entry = new MeasurementEntry()
                .id(e.getId())
                .userId(e.getUserId())
                .date(e.getDate())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        if (e.getWaistCm() != null) entry.setWaistCm(e.getWaistCm().doubleValue());
        if (e.getHipCm() != null) entry.setHipCm(e.getHipCm().doubleValue());
        if (e.getChestCm() != null) entry.setChestCm(e.getChestCm().doubleValue());
        if (e.getArmCm() != null) entry.setArmCm(e.getArmCm().doubleValue());
        if (e.getThighCm() != null) entry.setThighCm(e.getThighCm().doubleValue());
        return entry;
    }

    private StepEntry toStepEntry(StepEntryEntity e) {
        return new StepEntry()
                .id(e.getId())
                .userId(e.getUserId())
                .date(e.getDate())
                .steps(e.getSteps())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
    }

    private HydrationEntry toHydrationEntry(HydrationEntryEntity e) {
        return new HydrationEntry()
                .id(e.getId())
                .userId(e.getUserId())
                .date(e.getDate())
                .ml(e.getMl())
                .source(e.getSource())
                .loggedAt(e.getLoggedAt() != null ? e.getLoggedAt().atOffset(ZoneOffset.UTC) : null);
    }
}
