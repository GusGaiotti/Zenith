package com.gaiotti.zenith.repository;

import com.gaiotti.zenith.model.Category;
import com.gaiotti.zenith.model.Ledger;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.service.TransactionSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Ledger testLedger;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        ledgerRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashed")
                .displayName("Test User")
                .build();
        testUser = userRepository.save(testUser);

        testLedger = Ledger.builder()
                .name("Test Ledger")
                .build();
        testLedger = ledgerRepository.save(testLedger);

        testCategory = Category.builder()
                .name("Food")
                .color("#FF5733")
                .ledger(testLedger)
                .createdBy(testUser)
                .build();
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void findByIdAndLedgerId_ExistingTransaction_ReturnsTransaction() {
        Transaction transaction = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.now())
                .createdBy(testUser)
                .build();
        transaction = transactionRepository.save(transaction);

        var result = transactionRepository.findByIdAndLedgerId(transaction.getId(), testLedger.getId());

        assertTrue(result.isPresent());
        assertEquals(transaction.getId(), result.get().getId());
    }

    @Test
    void findByIdAndLedgerId_WrongLedger_ReturnsEmpty() {
        Ledger otherLedger = Ledger.builder().name("Other").build();
        otherLedger = ledgerRepository.save(otherLedger);

        Transaction transaction = Transaction.builder()
                .ledger(otherLedger)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.now())
                .createdBy(testUser)
                .build();
        transaction = transactionRepository.save(transaction);

        var result = transactionRepository.findByIdAndLedgerId(transaction.getId(), testLedger.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByCategoryId_WithTransactions_ReturnsTrue() {
        Transaction transaction = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.now())
                .category(testCategory)
                .createdBy(testUser)
                .build();
        transactionRepository.save(transaction);

        assertTrue(transactionRepository.existsByCategoryId(testCategory.getId()));
    }

    @Test
    void existsByCategoryId_NoTransactions_ReturnsFalse() {
        assertFalse(transactionRepository.existsByCategoryId(testCategory.getId()));
    }

    @Test
    void findAll_WithSpecification_FiltersByDateRange() {
        Transaction t1 = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.of(2024, 1, 15))
                .createdBy(testUser)
                .build();
        transactionRepository.save(t1);

        Transaction t2 = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(200))
                .type(Transaction.TransactionType.INCOME)
                .date(LocalDate.of(2024, 2, 15))
                .createdBy(testUser)
                .build();
        transactionRepository.save(t2);

        var spec = TransactionSpecification.withFilters(
                testLedger.getId(),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                null,
                null,
                null
        );

        var results = transactionRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals(LocalDate.of(2024, 1, 15), results.get(0).getDate());
    }

    @Test
    void findAll_WithSpecification_FiltersByType() {
        Transaction t1 = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.now())
                .createdBy(testUser)
                .build();
        transactionRepository.save(t1);

        Transaction t2 = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(200))
                .type(Transaction.TransactionType.INCOME)
                .date(LocalDate.now())
                .createdBy(testUser)
                .build();
        transactionRepository.save(t2);

        var spec = TransactionSpecification.withFilters(
                testLedger.getId(),
                null,
                null,
                null,
                null,
                Transaction.TransactionType.EXPENSE
        );

        var results = transactionRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals(Transaction.TransactionType.EXPENSE, results.get(0).getType());
    }

    @Test
    void findAll_WithSpecification_FiltersByCategory() {
        Category otherCategory = Category.builder()
                .name("Transport")
                .color("#00FF00")
                .ledger(testLedger)
                .createdBy(testUser)
                .build();
        otherCategory = categoryRepository.save(otherCategory);

        Transaction t1 = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.now())
                .category(testCategory)
                .createdBy(testUser)
                .build();
        transactionRepository.save(t1);

        Transaction t2 = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(200))
                .type(Transaction.TransactionType.INCOME)
                .date(LocalDate.now())
                .category(otherCategory)
                .createdBy(testUser)
                .build();
        transactionRepository.save(t2);

        var spec = TransactionSpecification.withFilters(
                testLedger.getId(),
                null,
                null,
                testCategory.getId(),
                null,
                null
        );

        var results = transactionRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals(testCategory.getId(), results.get(0).getCategory().getId());
    }

    @Test
    void findAll_WithSpecification_LedgerIsolation() {
        Ledger otherLedger = Ledger.builder().name("Other Ledger").build();
        otherLedger = ledgerRepository.save(otherLedger);

        Transaction t1 = Transaction.builder()
                .ledger(testLedger)
                .amount(BigDecimal.valueOf(100))
                .type(Transaction.TransactionType.EXPENSE)
                .date(LocalDate.now())
                .createdBy(testUser)
                .build();
        transactionRepository.save(t1);

        Transaction t2 = Transaction.builder()
                .ledger(otherLedger)
                .amount(BigDecimal.valueOf(200))
                .type(Transaction.TransactionType.INCOME)
                .date(LocalDate.now())
                .createdBy(testUser)
                .build();
        transactionRepository.save(t2);

        var spec = TransactionSpecification.withFilters(
                testLedger.getId(),
                null,
                null,
                null,
                null,
                null
        );

        var results = transactionRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals(testLedger.getId(), results.get(0).getLedger().getId());
    }
}
