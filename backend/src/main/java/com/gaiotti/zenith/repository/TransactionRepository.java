package com.gaiotti.zenith.repository;

import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.Transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndLedgerId(Long id, Long ledgerId);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.ledger.id = :ledgerId AND t.type = :type " +
           "AND t.date >= :startDate AND t.date <= :endDate " +
           "AND (:createdByUserId IS NULL OR t.createdBy.id = :createdByUserId)")
    BigDecimal sumAmountByLedgerAndTypeAndDateRange(
            @Param("ledgerId") Long ledgerId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query("SELECT t.category.id, t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.ledger.id = :ledgerId AND t.type = 'EXPENSE' " +
           "AND t.date >= :startDate AND t.date <= :endDate " +
           "AND (:createdByUserId IS NULL OR t.createdBy.id = :createdByUserId) " +
           "AND t.category IS NOT NULL " +
           "GROUP BY t.category.id, t.category.name")
    List<Object[]> sumExpensesByCategoryForLedgerAndDateRange(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query("SELECT t.createdBy.id, t.createdBy.email, SUM(t.amount) FROM Transaction t " +
           "WHERE t.ledger.id = :ledgerId AND t.type = 'EXPENSE' " +
           "AND t.date >= :startDate AND t.date <= :endDate " +
           "AND (:createdByUserId IS NULL OR t.createdBy.id = :createdByUserId) " +
           "GROUP BY t.createdBy.id, t.createdBy.email")
    List<Object[]> sumExpensesByUserForLedgerAndDateRange(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query(value = """
        SELECT 
            u.id as user_id, u.email, u.display_name,
            COALESCE(SUM(CASE
                WHEN :createdByUserId IS NOT NULL AND u.id <> :createdByUserId THEN 0
                WHEN t.type = 'INCOME' AND t.date >= :startDate AND t.date <= :endDate
                THEN ABS(t.amount) ELSE 0 END), 0) as total_income,
            COALESCE(SUM(CASE
                WHEN :createdByUserId IS NOT NULL AND u.id <> :createdByUserId THEN 0
                WHEN t.type = 'EXPENSE' AND t.date >= :startDate AND t.date <= :endDate
                THEN ABS(t.amount) ELSE 0 END), 0) as total_expense
        FROM ledger_members lm
        JOIN users u ON lm.user_id = u.id
        LEFT JOIN transactions t ON t.created_by = u.id AND t.ledger_id = lm.ledger_id
        WHERE lm.ledger_id = :ledgerId
        GROUP BY u.id, u.email, u.display_name
        """, nativeQuery = true)
    List<Object[]> getCoupleDynamics(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query(value = """
        SELECT 
            EXTRACT(YEAR FROM t.date) as year,
            EXTRACT(MONTH FROM t.date) as month,
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expense
        FROM transactions t
        WHERE t.ledger_id = :ledgerId AND t.date >= :startDate AND t.date <= :endDate
          AND (:createdByUserId IS NULL OR t.created_by = :createdByUserId)
        GROUP BY EXTRACT(YEAR FROM t.date), EXTRACT(MONTH FROM t.date)
        ORDER BY year, month
        """, nativeQuery = true)
    List<Object[]> getMonthlyTrends(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query(value = """
        SELECT 
            c.id as category_id, c.name, c.color,
            COALESCE(SUM(t.amount), 0) as total_spent,
            COUNT(t.id) as transaction_count,
            COALESCE(AVG(t.amount), 0) as avg_transaction
        FROM transactions t
        JOIN categories c ON t.category_id = c.id
        WHERE t.ledger_id = :ledgerId AND t.type = 'EXPENSE' 
            AND t.date >= :startDate AND t.date <= :endDate
            AND (:createdByUserId IS NULL OR t.created_by = :createdByUserId)
        GROUP BY c.id, c.name, c.color
        ORDER BY total_spent DESC
        """, nativeQuery = true)
    List<Object[]> getCategoryBreakdown(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query(value = """
        SELECT COALESCE(SUM(t.amount), 0) as total, COUNT(t.id) as count
        FROM transactions t
        WHERE t.ledger_id = :ledgerId AND t.type = 'EXPENSE' 
            AND t.date >= :startDate AND t.date <= :endDate
            AND (:createdByUserId IS NULL OR t.created_by = :createdByUserId)
            AND t.category_id IS NULL
        """, nativeQuery = true)
    Object[] getUncategorizedTotals(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query(value = """
        SELECT t.date, COALESCE(SUM(t.amount), 0) as daily_expense
        FROM transactions t
        WHERE t.ledger_id = :ledgerId AND t.type = 'EXPENSE' 
            AND t.date >= :startDate AND t.date <= :endDate
            AND (:createdByUserId IS NULL OR t.created_by = :createdByUserId)
        GROUP BY t.date
        ORDER BY t.date
        """, nativeQuery = true)
    List<Object[]> getDailySpending(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query(value = """
        SELECT t.amount as max_amount, u.display_name
        FROM transactions t
        JOIN users u ON t.created_by = u.id
        WHERE t.ledger_id = :ledgerId AND t.type = 'EXPENSE' 
            AND t.date >= :startDate AND t.date <= :endDate
            AND (:createdByUserId IS NULL OR t.created_by = :createdByUserId)
        ORDER BY ABS(t.amount) DESC, t.date DESC, t.id DESC
        LIMIT 1
        """, nativeQuery = true)
    Object[] getHighestTransaction(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("createdByUserId") Long createdByUserId
    );

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.category " +
           "LEFT JOIN FETCH t.createdBy " +
           "WHERE t.ledger.id = :ledgerId " +
           "AND t.date >= :startDate AND t.date <= :endDate " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findSampleForLedgerAndDateRange(
            @Param("ledgerId") Long ledgerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
