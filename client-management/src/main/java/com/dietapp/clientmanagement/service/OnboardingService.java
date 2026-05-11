package com.dietapp.clientmanagement.service;

import com.dietapp.api.clientmgmt.model.OnboardingProfile;
import com.dietapp.api.clientmgmt.model.OnboardingRequest;
import com.dietapp.clientmanagement.model.entity.OnboardingEntity;
import com.dietapp.clientmanagement.repository.OnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingRepository onboardingRepository;

    @Transactional(readOnly = true)
    public Optional<OnboardingProfile> getProfile(Long userId) {
        return onboardingRepository.findByUserId(userId).map(this::toResponse);
    }

    @Transactional
    public OnboardingProfile saveProfile(Long userId, OnboardingRequest request) {
        OnboardingEntity entity = onboardingRepository.findByUserId(userId)
                .orElseGet(() -> OnboardingEntity.builder().userId(userId).build());

        if (request.getHealthConditions() != null)
            entity.setHealthConditions(String.join(",", request.getHealthConditions()));
        if (request.getAllergies() != null)
            entity.setAllergies(request.getAllergies());
        if (request.getDietaryPreference() != null)
            entity.setDietaryPreference(request.getDietaryPreference().getValue());
        if (request.getGoal() != null)
            entity.setGoal(request.getGoal());
        if (request.getAge() != null)
            entity.setAge(request.getAge());
        if (request.getGender() != null)
            entity.setGender(request.getGender().getValue());
        if (request.getHeightCm() != null)
            entity.setHeightCm(BigDecimal.valueOf(request.getHeightCm()));
        if (request.getWeightKg() != null) {
            entity.setWeightKg(BigDecimal.valueOf(request.getWeightKg()));
            if (request.getHeightCm() != null || entity.getHeightCm() != null) {
                double height = entity.getHeightCm() != null ? entity.getHeightCm().doubleValue() : request.getHeightCm();
                double bmi = (request.getWeightKg() / (height / 100.0) / (height / 100.0));
                entity.setBmiValue(BigDecimal.valueOf(bmi).setScale(2, RoundingMode.HALF_UP));
                entity.setBmiCategory(bmiCategory(bmi));
            }
        }

        boolean isComplete = entity.getGoal() != null && entity.getAge() != null
                && entity.getGender() != null && entity.getHeightCm() != null
                && entity.getWeightKg() != null;
        entity.setComplete(isComplete);

        return toResponse(onboardingRepository.save(entity));
    }

    private String bmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    private OnboardingProfile toResponse(OnboardingEntity e) {
        List<String> conditions = e.getHealthConditions() != null && !e.getHealthConditions().isBlank()
                ? Arrays.asList(e.getHealthConditions().split(","))
                : List.of();

        OnboardingProfile p = new OnboardingProfile()
                .id(e.getId())
                .userId(e.getUserId())
                .healthConditions(conditions)
                .allergies(e.getAllergies())
                .dietaryPreference(e.getDietaryPreference() != null
                        ? OnboardingProfile.DietaryPreferenceEnum.fromValue(e.getDietaryPreference()) : null)
                .goal(e.getGoal())
                .age(e.getAge())
                .gender(e.getGender() != null
                        ? OnboardingProfile.GenderEnum.fromValue(e.getGender()) : null)
                .complete(e.getComplete())
                .updatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);

        if (e.getHeightCm() != null) p.setHeightCm(e.getHeightCm().doubleValue());
        if (e.getWeightKg() != null) p.setWeightKg(e.getWeightKg().doubleValue());
        if (e.getBmiValue() != null)  p.setBmiValue(e.getBmiValue().doubleValue());
        p.setBmiCategory(e.getBmiCategory());

        return p;
    }
}
