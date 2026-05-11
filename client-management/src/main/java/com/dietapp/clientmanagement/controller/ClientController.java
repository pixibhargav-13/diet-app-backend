package com.dietapp.clientmanagement.controller;

import com.dietapp.api.clientmgmt.api.ClientsApi;
import com.dietapp.api.clientmgmt.model.*;
import com.dietapp.clientmanagement.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

    private final ClientService clientService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientSummary>> getAllClients(String search, String goal, String status, String sortBy) {
        return ResponseEntity.ok(clientService.getAllClients(search, goal, status, sortBy));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientProfile> getClientProfile(Long clientId) {
        return ResponseEntity.ok(clientService.getClientProfile(clientId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientProfile> updateClientProfile(Long clientId, UpdateClientRequest updateClientRequest) {
        return ResponseEntity.ok(clientService.updateClientProfile(clientId, updateClientRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientProfile> updateCompliance(Long clientId, ComplianceUpdateRequest complianceUpdateRequest) {
        return ResponseEntity.ok(clientService.updateCompliance(clientId, complianceUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientNote>> getClientNotes(Long clientId) {
        return ResponseEntity.ok(clientService.getClientNotes(clientId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientNote> addClientNote(Long clientId, ClientNoteRequest clientNoteRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.addClientNote(clientId, clientNoteRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MealFeedback> submitMealFeedback(Long clientId, MealFeedbackRequest mealFeedbackRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.submitMealFeedback(clientId, mealFeedbackRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardStats> getAdminDashboardStats() {
        return ResponseEntity.ok(clientService.getAdminDashboardStats());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityItem>> getActivityFeed(LocalDate date) {
        return ResponseEntity.ok(clientService.getActivityFeed(date));
    }
}
