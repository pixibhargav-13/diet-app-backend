package com.dietapp.sessionmanagement.controller;

import com.dietapp.api.sessionmanagement.api.PackagesApi;
import com.dietapp.api.sessionmanagement.model.CreatePackageRequest;
import com.dietapp.api.sessionmanagement.model.PackageResponse;
import com.dietapp.api.sessionmanagement.model.UpdatePackageRequest;
import com.dietapp.sessionmanagement.service.SessionPackageService;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SessionPackageController implements PackagesApi {

    private final SessionPackageService packageService;
    private final UserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllPackages());
    }

    @Override
    public ResponseEntity<List<PackageResponse>> getMyPackages() {
        return ResponseEntity.ok(packageService.getPackagesByClientId(currentUserId()));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PackageResponse>> getClientPackages(Long clientId) {
        return ResponseEntity.ok(packageService.getPackagesByClientId(clientId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PackageResponse> createPackage(CreatePackageRequest createPackageRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(packageService.createPackage(createPackageRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PackageResponse> updatePackage(Long id, UpdatePackageRequest updatePackageRequest) {
        return ResponseEntity.ok(packageService.updatePackage(id, updatePackageRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePackage(Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
    }
}
