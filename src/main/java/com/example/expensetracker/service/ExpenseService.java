package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    /**
     * Save or update an expense
     */
    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    /**
     * Get all expenses for a user ordered by date (most recent first)
     */
    public List<Expense> getAllExpensesForUser(User user) {
        return expenseRepository.findByUserOrderByDateDesc(user);
    }

    /**
     * Get a single expense by ID
     */
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    /**
     * Get expense by ID with Optional (safer version)
     */
    public Optional<Expense> findExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    /**
     * Delete an expense by ID
     */
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    /**
     * Delete an expense (entity)
     */
    public void deleteExpense(Expense expense) {
        expenseRepository.delete(expense);
    }

    /**
     * Calculate total income for a user
     */
    public BigDecimal calculateTotalIncome(User user) {
        BigDecimal total = expenseRepository.calculateTotalIncome(user);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total expenses for a user
     */
    public BigDecimal calculateTotalExpenses(User user) {
        BigDecimal total = expenseRepository.calculateTotalExpenses(user);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate balance (income - expenses) for a user
     */
    public BigDecimal calculateBalance(User user) {
        BigDecimal income = calculateTotalIncome(user);
        BigDecimal expenses = calculateTotalExpenses(user);
        return income.subtract(expenses);
    }

    /**
     * Get all expenses by category for a user
     */
    public List<Expense> getExpensesByCategory(User user, Expense.Category category) {
        return expenseRepository.findByUserAndCategory(user, category);
    }

    /**
     * Get all expenses by type (INCOME or EXPENSE) for a user
     */
    public List<Expense> getExpensesByType(User user, Expense.TransactionType type) {
        return expenseRepository.findByUserAndType(user, type);
    }

    /**
     * Check if an expense belongs to a specific user (for security)
     */
    public boolean isExpenseOwnedByUser(Long expenseId, User user) {
        Optional<Expense> expense = expenseRepository.findById(expenseId);
        return expense.isPresent() && expense.get().getUser().getId().equals(user.getId());
    }

    /**
     * Count total number of transactions for a user
     */
    public long countUserTransactions(User user) {
        return expenseRepository.findByUser(user).size();
    }

    /**
     * Get income transactions only
     */
    public List<Expense> getIncomeTransactions(User user) {
        return expenseRepository.findByUserAndType(user, Expense.TransactionType.INCOME);
    }

    /**
     * Get expense transactions only
     */
    public List<Expense> getExpenseTransactions(User user) {
        return expenseRepository.findByUserAndType(user, Expense.TransactionType.EXPENSE);
    }
}