package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.request.CreateCategoryRequest;
import com.gaiotti.zenith.dto.request.UpdateCategoryRequest;
import com.gaiotti.zenith.dto.response.CategoryResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.Category;
import com.gaiotti.zenith.model.Ledger;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private LedgerMemberRepository ledgerMemberRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Ledger testLedger;
    private Category testCategory;

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
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listCategories_Success() {
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findByLedgerId(1L)).thenReturn(List.of(testCategory));

        List<CategoryResponse> result = categoryService.listCategories(1L, testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getName());
    }

    @Test
    void listCategories_NotMember_ThrowsAccessDeniedException() {
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> categoryService.listCategories(1L, testUser));
    }

    @Test
    void createCategory_Success() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Food");
        request.setColor("#FF5733");

        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse result = categoryService.createCategory(1L, request, testUser);

        assertNotNull(result);
        assertEquals("Food", result.getName());
    }

    @Test
    void createCategory_LedgerNotFound_ThrowsResourceNotFoundException() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Food");
        request.setColor("#FF5733");

        when(ledgerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.createCategory(1L, request, testUser));
    }

    @Test
    void updateCategory_Success() {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Updated Food");
        request.setColor("#FF5733");

        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse result = categoryService.updateCategory(1L, 1L, request, testUser);

        assertNotNull(result);
    }

    @Test
    void updateCategory_CategoryNotFound_ThrowsResourceNotFoundException() {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Updated Food");
        request.setColor("#FF5733");

        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(1L, 1L, request, testUser));
    }

    @Test
    void deleteCategory_Success() {
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.existsByCategoryId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L, 1L, testUser));
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_HasTransactions_ThrowsIllegalArgumentException() {
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(categoryRepository.findByIdAndLedgerId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.deleteCategory(1L, 1L, testUser));
    }

    @Test
    void deleteCategory_NotMember_ThrowsAccessDeniedException() {
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(testLedger));
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> categoryService.deleteCategory(1L, 1L, testUser));
    }
}
