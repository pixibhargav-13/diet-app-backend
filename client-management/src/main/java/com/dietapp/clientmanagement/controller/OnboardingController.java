package com.dietapp.clientmanagement.controller;

import com.dietapp.api.clientmgmt.api.OnboardingApi;
import com.dietapp.api.clientmgmt.model.OnboardingProfile;
import com.dietapp.api.clientmgmt.model.OnboardingRequest;
import com.dietapp.clientmanagement.service.OnboardingService;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OnboardingController implements OnboardingApi {

    private final OnboardingService onboardingService;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<OnboardingProfile> getOnboardingProfile() {
        return onboardingService.getProfile(currentUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<OnboardingProfile> submitOnboarding(OnboardingRequest onboardingRequest) {
        return ResponseEntity.ok(onboardingService.saveProfile(currentUserId(), onboardingRequest));
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
    }
}
