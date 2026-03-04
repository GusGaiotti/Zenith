package com.gaiotti.zenith.repository;

import com.gaiotti.zenith.model.Category;
import com.gaiotti.zenith.model.Ledger;
import com.gaiotti.zenith.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private UserRepository userRepository;

    private Ledger testLedger;
    private User testUser;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void findByLedgerId_ReturnsOnlyCategoriesForThatLedger() {
        Category cat1 = Category.builder()
                .name("Food")
                .color("#FF5733")
                .ledger(testLedger)
                .createdBy(testUser)
                .build();
        categoryRepository.save(cat1);

        Ledger otherLedger = Ledger.builder().name("Other").build();
        otherLedger = ledgerRepository.save(otherLedger);

        Category cat2 = Category.builder()
                .name("Transport")
                .color("#00FF00")
                .ledger(otherLedger)
                .createdBy(testUser)
                .build();
        categoryRepository.save(cat2);

        List<Category> result = categoryRepository.findByLedgerId(testLedger.getId());

        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getName());
    }

    @Test
    void existsByIdAndLedgerId_CorrectLedger_ReturnsTrue() {
        Category category = Category.builder()
                .name("Food")
                .color("#FF5733")
                .ledger(testLedger)
                .createdBy(testUser)
                .build();
        category = categoryRepository.save(category);

        assertTrue(categoryRepository.existsByIdAndLedgerId(category.getId(), testLedger.getId()));
    }

    @Test
    void existsByIdAndLedgerId_WrongLedger_ReturnsFalse() {
        Ledger otherLedger = Ledger.builder().name("Other").build();
        otherLedger = ledgerRepository.save(otherLedger);

        Category category = Category.builder()
                .name("Food")
                .color("#FF5733")
                .ledger(otherLedger)
                .createdBy(testUser)
                .build();
        category = categoryRepository.save(category);

        assertFalse(categoryRepository.existsByIdAndLedgerId(category.getId(), testLedger.getId()));
    }
}
