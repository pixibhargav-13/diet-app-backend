package com.dietapp.clientmanagement.controller;

import com.dietapp.api.clientmgmt.api.RevenueApi;
import com.dietapp.api.clientmgmt.model.RevenueSummary;
import com.dietapp.api.clientmgmt.model.Transaction;
import com.dietapp.clientmanagement.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RevenueController implements RevenueApi {

    private final RevenueService revenueService;

    @Override
    public ResponseEntity<RevenueSummary> getRevenueSummary(LocalDate from, LocalDate to) {
        return ResponseEntity.ok(revenueService.getRevenueSummary(from, to));
    }

    @Override
    public ResponseEntity<List<Transaction>> getTransactions(LocalDate from, LocalDate to, String type) {
        return ResponseEntity.ok(revenueService.getTransactions(from, to, type));
    }
}
