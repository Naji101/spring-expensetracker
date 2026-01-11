package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.ExpenseService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public ExpenseController(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, 
            Authentication authentication) {
        
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all expenses for the user
        List<Expense> expenses = expenseService.getAllExpensesForUser(user);

        // Apply filters
        expenses = applyFilters(expenses, search, type, category, period, startDate, endDate);

        // Calculate totals (always based on ALL user's data, not filtered)
        BigDecimal totalIncome = expenseService.calculateTotalIncome(user);
        BigDecimal totalExpenses = expenseService.calculateTotalExpenses(user);
        BigDecimal balance = expenseService.calculateBalance(user);

        model.addAttribute("expenses", expenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("balance", balance);
        model.addAttribute("expense", new Expense());
        model.addAttribute("categories", Expense.Category.values());

        return "dashboard";
    }

    private List<Expense> applyFilters(List<Expense> expenses, String search, String type, 
                                       String category, String period, String startDate, String endDate) {
        
        // Filter by search (description)
        if (search != null && !search.trim().isEmpty()) {
            expenses = expenses.stream()
                    .filter(e -> e.getDescription().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter by type
        if (type != null && !type.isEmpty()) {
            Expense.TransactionType transactionType = Expense.TransactionType.valueOf(type);
            expenses = expenses.stream()
                    .filter(e -> e.getType() == transactionType)
                    .collect(Collectors.toList());
        }

        // Filter by category
        if (category != null && !category.isEmpty()) {
            Expense.Category expenseCategory = Expense.Category.valueOf(category);
            expenses = expenses.stream()
                    .filter(e -> e.getCategory() == expenseCategory)
                    .collect(Collectors.toList());
        }

        // Filter by date period
        if (period != null && !period.isEmpty()) {
            LocalDate start = null;
            LocalDate end = LocalDate.now();

            switch (period) {
                case "today":
                    start = LocalDate.now();
                    break;
                case "this_week":
                    start = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                    break;
                case "this_month":
                    start = LocalDate.now().withDayOfMonth(1);
                    break;
                case "last_month":
                    start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    end = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                    break;
                case "custom":
                    if (startDate != null && !startDate.isEmpty()) {
                        start = LocalDate.parse(startDate);
                    }
                    if (endDate != null && !endDate.isEmpty()) {
                        end = LocalDate.parse(endDate);
                    }
                    break;
            }

            if (start != null) {
                final LocalDate finalStart = start;
                final LocalDate finalEnd = end;
                expenses = expenses.stream()
                        .filter(e -> !e.getDate().isBefore(finalStart) && !e.getDate().isAfter(finalEnd))
                        .collect(Collectors.toList());
            }
        }

        return expenses;
    }

    @PostMapping("/expense/add")
    public String addExpense(@ModelAttribute Expense expense, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        expense.setUser(user);
        expenseService.saveExpense(expense);

        return "redirect:/dashboard";
    }

    @PostMapping("/expense/delete/{id}")
    public String deleteExpense(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = expenseService.getExpenseById(id);
        
        // Security check: ensure user can only delete their own expenses
        if (expense.getUser().getId().equals(user.getId())) {
            expenseService.deleteExpense(id);
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/expense/edit/{id}")
    public String editExpense(@PathVariable Long id, Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = expenseService.getExpenseById(id);

        // Security check: ensure user can only edit their own expenses
        if (!expense.getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("expense", expense);
        model.addAttribute("categories", Expense.Category.values());
        
        return "edit-expense";
    }

    @PostMapping("/expense/update/{id}")
    public String updateExpense(@PathVariable Long id, @ModelAttribute Expense expenseDetails, 
                                Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = expenseService.getExpenseById(id);

        // Security check
        if (!expense.getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }

        expense.setDescription(expenseDetails.getDescription());
        expense.setAmount(expenseDetails.getAmount());
        expense.setType(expenseDetails.getType());
        expense.setCategory(expenseDetails.getCategory());
        expense.setDate(expenseDetails.getDate());
        expense.setNotes(expenseDetails.getNotes());

        expenseService.saveExpense(expense);

        return "redirect:/dashboard";
    }
}