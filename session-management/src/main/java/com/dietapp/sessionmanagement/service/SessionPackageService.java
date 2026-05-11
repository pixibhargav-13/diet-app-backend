package com.dietapp.sessionmanagement.service;

import com.dietapp.api.sessionmanagement.model.CreatePackageRequest;
import com.dietapp.api.sessionmanagement.model.PackageResponse;
import com.dietapp.api.sessionmanagement.model.UpdatePackageRequest;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.sessionmanagement.model.entity.SessionPackageEntity;
import com.dietapp.sessionmanagement.repository.SessionPackageRepository;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionPackageService {

    private final SessionPackageRepository packageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages() {
        return packageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> toResponse(p, resolveClientName(p.getClientId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackageResponse> getPackagesByClientId(Long clientId) {
        return packageRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(p -> toResponse(p, resolveClientName(clientId)))
                .toList();
    }

    @Transactional
    public PackageResponse createPackage(CreatePackageRequest request) {
        String clientName = resolveClientName(request.getClientId());
        SessionPackageEntity entity = SessionPackageEntity.builder()
                .clientId(request.getClientId())
                .packageName(request.getPackageName())
                .sessionCount(request.getSessionCount())
                .price(request.getPrice())
                .sessionsUsed(0)
                .active(true)
                .build();
        return toResponse(packageRepository.save(entity), clientName);
    }

    @Transactional
    public PackageResponse updatePackage(Long id, UpdatePackageRequest request) {
        SessionPackageEntity entity = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found: " + id));

        if (request.getPackageName() != null) entity.setPackageName(request.getPackageName());
        if (request.getSessionCount() != null) entity.setSessionCount(request.getSessionCount());
        if (request.getPrice() != null) entity.setPrice(request.getPrice());
        if (request.getActive() != null) entity.setActive(request.getActive());

        return toResponse(packageRepository.save(entity), resolveClientName(entity.getClientId()));
    }

    @Transactional
    public void deletePackage(Long id) {
        if (!packageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Package not found: " + id);
        }
        packageRepository.deleteById(id);
    }

    private String resolveClientName(Long clientId) {
        return userRepository.findById(clientId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");
    }

    private PackageResponse toResponse(SessionPackageEntity e, String clientName) {
        return new PackageResponse()
                .id(e.getId())
                .clientId(e.getClientId())
                .clientName(clientName)
                .packageName(e.getPackageName())
                .sessionCount(e.getSessionCount())
                .sessionsUsed(e.getSessionsUsed())
                .sessionsRemaining(e.getSessionCount() - e.getSessionsUsed())
                .price(e.getPrice())
                .active(e.getActive())
                .createdAt(e.getCreatedAt() != null
                        ? e.getCreatedAt().atOffset(java.time.ZoneOffset.UTC)
                        : null);
    }
}
