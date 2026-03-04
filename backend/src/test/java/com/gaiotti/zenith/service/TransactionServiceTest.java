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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private LedgerMemberRepository ledgerMemberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Ledger testLedger;
    private Category testCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        testLedger = Ledger.builder()
                .id(1L)
                .name("Test Ledger")
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Food")
                .color("#FF5733")
                .ledger(testLedger)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.now())
                .ledger(testLedger)
                .category(testCategory)
                .description("Test")
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTransaction_Success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(Transaction.TransactionType.EXPENSE);
        request.setDate(LocalDate.now());
        request.setDescription("Test");

        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionResponse result = transactionService.createTransaction(1L, request, testUser);

        assertNotNull(result);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_WithCategory_Success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(Transaction.TransactionType.EXPENSE);
        request.setDate(LocalDate.now());
        request.setCategoryId(1L);
        request.setDescription("Test");

        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionResponse result = transactionService.createTransaction(1L, request, testUser);

        assertNotNull(result);
    }

    @Test
    void createTransaction_CategoryFromDifferentLedger_ThrowsIllegalArgumentException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(Transaction.TransactionType.EXPENSE);
        request.setDate(LocalDate.now());
        request.setCategoryId(1L);

        Ledger otherLedger = Ledger.builder().id(99L).build();
        Category otherCategory = Category.builder().id(1L).ledger(otherLedger).build();

        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(otherCategory));

        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(1L, request, testUser));
    }

    @Test
    void createTransaction_NotMember_ThrowsAccessDeniedException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(Transaction.TransactionType.EXPENSE);
        request.setDate(LocalDate.now());

        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> transactionService.createTransaction(1L, request, testUser));
    }

    @Test
    void getTransaction_Success() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.of(testTransaction));

        TransactionResponse result = transactionService.getTransaction(1L, 1L, testUser);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
    }

    @Test
    void getTransaction_NotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransaction(1L, 1L, testUser));
    }

    @Test
    void updateTransaction_Success() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(200));

        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionResponse result = transactionService.updateTransaction(1L, 1L, request, testUser);

        assertNotNull(result);
    }

    @Test
    void updateTransaction_DoesNotClearCategory_WhenCategoryIdIsOmitted() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setCategoryId(null);

        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse result = transactionService.updateTransaction(1L, 1L, request, testUser);

        assertNotNull(result);
        assertEquals(1L, result.getCategoryId());
    }

    @Test
    void deleteTransaction_Success() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.of(testTransaction));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, 1L, testUser));
        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    void deleteTransaction_NotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(1L, 1L, testUser));
    }

    @Test
    void listTransactions_NoFilters_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction), pageable, 1);

        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<TransactionResponse> result = transactionService.listTransactions(
                1L, testUser, null, null, null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
