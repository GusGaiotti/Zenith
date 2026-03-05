package com.gaiotti.zenith.service;

import com.gaiotti.zenith.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> withFilters(
            Long ledgerId,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            Long createdByUserId,
            Transaction.TransactionType type
    ) {
        return Specification.allOf(
                hasLedgerId(ledgerId),
                hasStartDate(startDate),
                hasEndDate(endDate),
                hasCategoryId(categoryId),
                hasCreatedByUserId(createdByUserId),
                hasType(type)
        );
    }

    private static Specification<Transaction> hasLedgerId(Long ledgerId) {
        return (root, query, cb) -> cb.equal(root.get("ledger").get("id"), ledgerId);
    }

    private static Specification<Transaction> hasStartDate(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("date"), startDate);
    }

    private static Specification<Transaction> hasEndDate(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("date"), endDate);
    }

    private static Specification<Transaction> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<Transaction> hasCreatedByUserId(Long createdByUserId) {
        return (root, query, cb) -> createdByUserId == null ? null : cb.equal(root.get("createdBy").get("id"), createdByUserId);
    }

    private static Specification<Transaction> hasType(Transaction.TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }
}
