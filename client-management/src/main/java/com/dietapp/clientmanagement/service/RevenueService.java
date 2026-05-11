package com.dietapp.clientmanagement.service;

import com.dietapp.api.clientmgmt.model.RevenueSummary;
import com.dietapp.api.clientmgmt.model.Transaction;
import com.dietapp.clientmanagement.model.entity.TransactionEntity;
import com.dietapp.clientmanagement.repository.TransactionRepository;
import com.dietapp.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public RevenueSummary getRevenueSummary(LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate effectiveTo   = to != null ? to : LocalDate.now();

        Instant start = effectiveFrom.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end   = effectiveTo.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<TransactionEntity> txns = transactionRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        double total = txns.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum();

        // Monthly
        Instant monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        double monthly = txns.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(monthStart))
                .mapToDouble(t -> t.getAmount().doubleValue()).sum();

        Map<String, Double> byMethod = txns.stream()
                .filter(t -> t.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(TransactionEntity::getPaymentMethod,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())));

        return new RevenueSummary()
                .totalRevenue(BigDecimal.valueOf(total))
                .monthlyRevenue(BigDecimal.valueOf(monthly))
                .monthOverMonthChange(BigDecimal.ZERO)
                .byPaymentMethod(byMethod)
                .periodFrom(effectiveFrom)
                .periodTo(effectiveTo);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(LocalDate from, LocalDate to, String type) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate effectiveTo   = to != null ? to : LocalDate.now();

        Instant start = effectiveFrom.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end   = effectiveTo.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<TransactionEntity> entities = type != null
                ? transactionRepository.findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(type, start, end)
                : transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        return entities.stream().map(this::toTransaction).toList();
    }

    private Transaction toTransaction(TransactionEntity e) {
        String clientName = userRepository.findById(e.getClientId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");

        Transaction t = new Transaction()
                .id(e.getId())
                .clientId(e.getClientId())
                .clientName(clientName)
                .amount(e.getAmount())
                .paymentMethod(e.getPaymentMethod())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null);

        if (e.getType() != null) {
            try {
                t.setType(Transaction.TypeEnum.fromValue(e.getType()));
            } catch (Exception ignored) {}
        }
        return t;
    }
}
