package com.dietapp.sessionmanagement.service;

import com.dietapp.api.sessionmanagement.model.PolicyResponse;
import com.dietapp.api.sessionmanagement.model.UpdatePolicyRequest;
import com.dietapp.sessionmanagement.exception.ResourceNotFoundException;
import com.dietapp.sessionmanagement.model.entity.SessionPolicyEntity;
import com.dietapp.sessionmanagement.repository.SessionPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionPolicyService {

    private final SessionPolicyRepository policyRepository;

    @Transactional(readOnly = true)
    public PolicyResponse getGlobalPolicy() {
        return policyRepository.findByClientIdIsNull()
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Global policy not found"));
    }

    @Transactional
    public PolicyResponse updateGlobalPolicy(UpdatePolicyRequest request) {
        SessionPolicyEntity policy = policyRepository.findByClientIdIsNull()
                .orElseGet(() -> SessionPolicyEntity.builder().build());

        if (request.getCancellationNoticeHours() != null)
            policy.setCancellationNoticeHours(request.getCancellationNoticeHours());
        if (request.getMaxReschedulePerMonth() != null)
            policy.setMaxReschedulePerMonth(request.getMaxReschedulePerMonth());

        return toResponse(policyRepository.save(policy));
    }

    /** Returns the effective policy for a client: override if exists, otherwise global. */
    @Transactional(readOnly = true)
    public PolicyResponse getEffectivePolicyForClient(Long clientId) {
        return policyRepository.findByClientId(clientId)
                .map(this::toResponse)
                .orElseGet(this::getGlobalPolicy);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getClientOverride(Long clientId) {
        return policyRepository.findByClientId(clientId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public PolicyResponse setClientOverride(Long clientId, UpdatePolicyRequest request) {
        SessionPolicyEntity policy = policyRepository.findByClientId(clientId)
                .orElseGet(() -> SessionPolicyEntity.builder().clientId(clientId).build());

        SessionPolicyEntity global = policyRepository.findByClientIdIsNull()
                .orElseThrow(() -> new ResourceNotFoundException("Global policy not found"));

        policy.setCancellationNoticeHours(
                request.getCancellationNoticeHours() != null
                        ? request.getCancellationNoticeHours()
                        : global.getCancellationNoticeHours());
        policy.setMaxReschedulePerMonth(
                request.getMaxReschedulePerMonth() != null
                        ? request.getMaxReschedulePerMonth()
                        : global.getMaxReschedulePerMonth());

        return toResponse(policyRepository.save(policy));
    }

    @Transactional
    public void clearClientOverride(Long clientId) {
        policyRepository.findByClientId(clientId).ifPresent(policyRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getAllClientOverrides() {
        return policyRepository.findByClientIdIsNotNull().stream()
                .map(this::toResponse)
                .toList();
    }

    private PolicyResponse toResponse(SessionPolicyEntity e) {
        return new PolicyResponse()
                .id(e.getId())
                .clientId(e.getClientId())
                .cancellationNoticeHours(e.getCancellationNoticeHours())
                .maxReschedulePerMonth(e.getMaxReschedulePerMonth())
                .global(e.getClientId() == null);
    }
}
