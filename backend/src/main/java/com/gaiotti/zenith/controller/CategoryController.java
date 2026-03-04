package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.request.CreateCategoryRequest;
import com.gaiotti.zenith.dto.request.UpdateCategoryRequest;
import com.gaiotti.zenith.dto.response.CategoryResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledgers/{ledgerId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories(@PathVariable Long ledgerId) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        List<CategoryResponse> categories = categoryService.listCategories(ledgerId, authenticatedUser);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable Long ledgerId,
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        CategoryResponse response = categoryService.createCategory(ledgerId, request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long ledgerId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        CategoryResponse response = categoryService.updateCategory(ledgerId, id, request, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long ledgerId,
            @PathVariable Long id
    ) {
        User authenticatedUser = authUtils.getAuthenticatedUser();
        categoryService.deleteCategory(ledgerId, id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
