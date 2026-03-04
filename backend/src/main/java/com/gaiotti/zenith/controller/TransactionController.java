package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.request.CreateTransactionRequest;
import com.gaiotti.zenith.dto.request.UpdateTransactionRequest;
import com.gaiotti.zenith.dto.response.PageResponse;
import com.gaiotti.zenith.dto.response.TransactionResponse;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ledgers/{ledgerId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> listTransactions(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) Transaction.TransactionType type,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        PageResponse<TransactionResponse> response = transactionService.listTransactions(
                ledgerId, authenticatedUser, startDate, endDate, categoryId, createdBy, type, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/months/{yearMonth}")
    public ResponseEntity<List<TransactionResponse>> getMonthlyTransactions(
            @PathVariable Long ledgerId,
            @PathVariable YearMonth yearMonth
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        List<TransactionResponse> response = transactionService.getMonthlyTransactions(ledgerId, yearMonth, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable Long ledgerId,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        TransactionResponse response = transactionService.createTransaction(ledgerId, request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long ledgerId,
            @PathVariable Long id
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        TransactionResponse response = transactionService.getTransaction(ledgerId, id, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long ledgerId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        TransactionResponse response = transactionService.updateTransaction(ledgerId, id, request, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long ledgerId,
            @PathVariable Long id
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        transactionService.deleteTransaction(ledgerId, id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
