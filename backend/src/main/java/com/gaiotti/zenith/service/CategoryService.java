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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(Long ledgerId, User authenticatedUser) {
        requireLedgerMembership(ledgerId, authenticatedUser);

        return categoryRepository.findByLedgerId(ledgerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponse createCategory(Long ledgerId, CreateCategoryRequest request, User authenticatedUser) {
        Ledger ledger = requireLedgerMembership(ledgerId, authenticatedUser);

        Category category = Category.builder()
                .ledger(ledger)
                .name(request.getName())
                .color(request.getColor())
                .createdBy(authenticatedUser)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public CategoryResponse updateCategory(Long ledgerId, Long categoryId, UpdateCategoryRequest request, User authenticatedUser) {
        requireLedgerMembership(ledgerId, authenticatedUser);

        Category category = categoryRepository.findByIdAndLedgerId(categoryId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setColor(request.getColor());

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public void deleteCategory(Long ledgerId, Long categoryId, User authenticatedUser) {
        requireLedgerMembership(ledgerId, authenticatedUser);

        Category category = categoryRepository.findByIdAndLedgerId(categoryId, ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new IllegalArgumentException("Cannot delete category: it has associated transactions");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .createdByUserId(category.getCreatedBy().getId())
                .createdByDisplayName(category.getCreatedBy().getDisplayName())
                .build();
    }

    private Ledger requireLedgerMembership(Long ledgerId, User authenticatedUser) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger not found"));

        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, authenticatedUser.getId())) {
            throw new AccessDeniedException("You are not a member of this ledger");
        }

        return ledger;
    }
}
