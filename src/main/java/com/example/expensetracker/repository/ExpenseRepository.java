package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses for a specific user, ordered by date descending (most recent first)
     */
    List<Expense> findByUserOrderByDateDesc(User user);

    /**
     * Find all expenses for a specific user
     */
    List<Expense> findByUser(User user);

    /**
     * Find expenses for a user ordered by amount descending
     */
    List<Expense> findByUserOrderByAmountDesc(User user);

    /**
     * Calculate total income for a user
     * Uses COALESCE to return 0 if no records found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.type = 'INCOME'")
    BigDecimal calculateTotalIncome(@Param("user") User user);

    /**
     * Calculate total expenses for a user
     * Uses COALESCE to return 0 if no records found
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.type = 'EXPENSE'")
    BigDecimal calculateTotalExpenses(@Param("user") User user);

    /**
     * Find expenses by category for a user
     */
    List<Expense> findByUserAndCategory(User user, Expense.Category category);

    /**
     * Find expenses by type (INCOME or EXPENSE) for a user
     */
    List<Expense> findByUserAndType(User user, Expense.TransactionType type);

    /**
     * Find expenses by type for a user, ordered by date descending
     */
    List<Expense> findByUserAndTypeOrderByDateDesc(User user, Expense.TransactionType type);

    /**
     * Find expenses between two dates for a user
     */
    List<Expense> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses by user and category, ordered by date descending
     */
    List<Expense> findByUserAndCategoryOrderByDateDesc(User user, Expense.Category category);

    /**
     * Find expenses greater than or equal to a specific amount
     */
    List<Expense> findByUserAndAmountGreaterThanEqual(User user, BigDecimal amount);

    /**
     * Find expenses less than or equal to a specific amount
     */
    List<Expense> findByUserAndAmountLessThanEqual(User user, BigDecimal amount);

    /**
     * Search expenses by description (case-insensitive partial match)
     */
    List<Expense> findByUserAndDescriptionContainingIgnoreCase(User user, String keyword);

    /**
     * Count total expenses for a user
     */
    long countByUser(User user);

    /**
     * Count expenses by type for a user
     */
    long countByUserAndType(User user, Expense.TransactionType type);

    /**
     * Find the most recent N expenses for a user
     */
    List<Expense> findTop10ByUserOrderByDateDesc(User user);

    /**
     * Calculate total for a specific category
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.category = :category")
    BigDecimal calculateTotalByCategory(@Param("user") User user, @Param("category") Expense.Category category);

    /**
     * Find expenses for current month
     */
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND YEAR(e.date) = :year AND MONTH(e.date) = :month ORDER BY e.date DESC")
    List<Expense> findByUserAndMonth(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    /**
     * Calculate monthly income
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.type = 'INCOME' AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal calculateMonthlyIncome(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    /**
     * Calculate monthly expenses
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.type = 'EXPENSE' AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    BigDecimal calculateMonthlyExpenses(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    /**
     * Delete all expenses for a user
     */
    void deleteByUser(User user);

    /**
     * Check if user has any expenses
     */
    boolean existsByUser(User user);
}