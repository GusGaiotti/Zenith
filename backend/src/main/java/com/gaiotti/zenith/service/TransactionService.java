package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.request.CreateTransactionRequest;
import com.gaiotti.zenith.dto.request.UpdateTransactionRequest;
import com.gaiotti.zenith.dto.response.PageResponse;
import com.gaiotti.zenith.dto.response.TransactionResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.Category;
import com.gaiotti.zenith.model.Ledger;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.CategoryRepository;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import com.gaiotti.zenith.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> listTransactions(
            Long ledgerId,
            User authenticatedUser,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            Long createdByUserId,
            Transaction.TransactionType type,
            Pageable pageable
    ) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        var spec = TransactionSpecification.withFilters(
                ledgerId, startDate, endDate, categoryId, createdByUserId, type
        );

        Page<Transaction> page = transactionRepository.findAll(spec, pageable);

        List<TransactionResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMonthlyTransactions(Long ledgerId, YearMonth yearMonth, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        var spec = TransactionSpecification.withFilters(
                ledgerId, startDate, endDate, null, null, null
        );

        return transactionRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TransactionResponse createTransaction(Long ledgerId, CreateTransactionRequest request, User authenticatedUser) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));
        assertMembership(ledgerId, authenticatedUser);

        Category category = resolveCategory(request.getCategoryId(), ledgerId);

        Transaction transaction = Transaction.builder()
                .ledger(ledger)
                .amount(request.getAmount())
                .type(request.getType())
                .date(request.getDate())
                .category(category)
                .description(request.getDescription())
                .createdBy(authenticatedUser)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        notificationService.createTransactionNotifications(savedTransaction, authenticatedUser);
        return mapToResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long ledgerId, Long transactionId, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        Transaction transaction = transactionRepository.findByIdAndLedgerId(transactionId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return mapToResponse(transaction);
    }

    public TransactionResponse updateTransaction(Long ledgerId, Long transactionId, UpdateTransactionRequest request, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        Transaction transaction = transactionRepository.findByIdAndLedgerId(transactionId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getDate() != null) {
            transaction.setDate(request.getDate());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null) {
            transaction.setCategory(resolveCategory(request.getCategoryId(), ledgerId));
        }

        return mapToResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long ledgerId, Long transactionId, User authenticatedUser) {
        assertLedgerExists(ledgerId);
        assertMembership(ledgerId, authenticatedUser);

        Transaction transaction = transactionRepository.findByIdAndLedgerId(transactionId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
    }

    private void assertLedgerExists(Long ledgerId) {
        if (!ledgerRepository.existsById(ledgerId)) {
            throw new ResourceNotFoundException("Ledger not found");
        }
    }

    private void assertMembership(Long ledgerId, User user) {
        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, user.getId())) {
            throw new AccessDeniedException("You are not a member of this ledger");
        }
    }

    private Category resolveCategory(Long categoryId, Long ledgerId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getLedger().getId().equals(ledgerId)) {
            throw new IllegalArgumentException("Category does not belong to this ledger");
        }
        return category;
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .date(transaction.getDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .description(transaction.getDescription())
                .createdByUserId(transaction.getCreatedBy().getId())
                .createdByDisplayName(transaction.getCreatedBy().getDisplayName())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
