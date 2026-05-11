package com.dietapp.sessionmanagement.controller;

import com.dietapp.api.sessionmanagement.api.PoliciesApi;
import com.dietapp.api.sessionmanagement.model.PolicyResponse;
import com.dietapp.api.sessionmanagement.model.UpdatePolicyRequest;
import com.dietapp.sessionmanagement.service.SessionPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SessionPolicyController implements PoliciesApi {

    private final SessionPolicyService policyService;

    @Override
    public ResponseEntity<PolicyResponse> getGlobalPolicy() {
        return ResponseEntity.ok(policyService.getGlobalPolicy());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponse> updateGlobalPolicy(UpdatePolicyRequest updatePolicyRequest) {
        return ResponseEntity.ok(policyService.updateGlobalPolicy(updatePolicyRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PolicyResponse>> getAllOverrides() {
        return ResponseEntity.ok(policyService.getAllClientOverrides());
    }

    @Override
    public ResponseEntity<PolicyResponse> getEffectivePolicy(Long clientId) {
        return ResponseEntity.ok(policyService.getEffectivePolicyForClient(clientId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponse> getClientOverride(Long clientId) {
        PolicyResponse override = policyService.getClientOverride(clientId);
        return override != null ? ResponseEntity.ok(override) : ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponse> setClientOverride(Long clientId, UpdatePolicyRequest updatePolicyRequest) {
        return ResponseEntity.ok(policyService.setClientOverride(clientId, updatePolicyRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearClientOverride(Long clientId) {
        policyService.clearClientOverride(clientId);
        return ResponseEntity.noContent().build();
    }
}
