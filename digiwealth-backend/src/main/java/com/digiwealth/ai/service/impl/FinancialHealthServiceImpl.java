package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.entity.Account;
import com.digiwealth.ai.entity.Goal;
import com.digiwealth.ai.entity.Investment;
import com.digiwealth.ai.entity.Transaction;
import com.digiwealth.ai.repository.AccountRepository;
import com.digiwealth.ai.repository.GoalRepository;
import com.digiwealth.ai.repository.InvestmentRepository;
import com.digiwealth.ai.repository.TransactionRepository;
import com.digiwealth.ai.service.FinancialHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes the 0-100 Financial Health Score using six weighted components:
 * Savings Ratio, Expense Ratio, Investment Ratio, Debt Ratio, Emergency Fund, Goal Progress.
 *
 * Each component is scored 0-100 individually, then combined using the weights below.
 * Weights are a reasonable default and can be tuned without changing the public contract.
 */
@Service
@RequiredArgsConstructor
public class FinancialHealthServiceImpl implements FinancialHealthService {

    private static final double W_SAVINGS = 0.20;
    private static final double W_EXPENSE = 0.15;
    private static final double W_INVESTMENT = 0.20;
    private static final double W_DEBT = 0.15;
    private static final double W_EMERGENCY_FUND = 0.15;
    private static final double W_GOAL_PROGRESS = 0.15;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final InvestmentRepository investmentRepository;
    private final GoalRepository goalRepository;

    @Override
    public Map<String, Object> calculateHealthScore(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpenses = BigDecimal.ZERO;
        BigDecimal loanEmiExpenses = BigDecimal.ZERO;

        for (Account account : accounts) {
            List<Transaction> monthTx = transactionRepository.findByAccountIdAndDateBetween(
                    account.getId(), monthStart, monthEnd);

            for (Transaction tx : monthTx) {
                if (tx.getTransactionType() == Transaction.TransactionType.CREDIT) {
                    monthlyIncome = monthlyIncome.add(tx.getAmount());
                } else {
                    monthlyExpenses = monthlyExpenses.add(tx.getAmount());
                    if (tx.getCategory() == Transaction.Category.LOAN_EMI) {
                        loanEmiExpenses = loanEmiExpenses.add(tx.getAmount());
                    }
                }
            }
        }

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Investment> investments = investmentRepository.findByUserId(userId);
        BigDecimal totalInvestments = investments.stream()
                .map(Investment::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Goal> goals = goalRepository.findByUserId(userId);

        double savingsRatioScore = scoreSavingsRatio(monthlyIncome, monthlyExpenses);
        double expenseRatioScore = scoreExpenseRatio(monthlyIncome, monthlyExpenses);
        double investmentRatioScore = scoreInvestmentRatio(monthlyIncome, totalInvestments);
        double debtRatioScore = scoreDebtRatio(monthlyIncome, loanEmiExpenses);
        double emergencyFundScore = scoreEmergencyFund(totalBalance, monthlyExpenses);
        double goalProgressScore = scoreGoalProgress(goals);

        double weighted =
                savingsRatioScore * W_SAVINGS +
                expenseRatioScore * W_EXPENSE +
                investmentRatioScore * W_INVESTMENT +
                debtRatioScore * W_DEBT +
                emergencyFundScore * W_EMERGENCY_FUND +
                goalProgressScore * W_GOAL_PROGRESS;

        int finalScore = (int) Math.round(Math.max(0, Math.min(100, weighted)));

        List<String> suggestions = buildSuggestions(
                savingsRatioScore, expenseRatioScore, investmentRatioScore,
                debtRatioScore, emergencyFundScore, goalProgressScore);

        Map<String, Object> result = new HashMap<>();
        result.put("score", finalScore);
        result.put("suggestions", suggestions);
        result.put("breakdown", Map.of(
                "savingsRatioScore", savingsRatioScore,
                "expenseRatioScore", expenseRatioScore,
                "investmentRatioScore", investmentRatioScore,
                "debtRatioScore", debtRatioScore,
                "emergencyFundScore", emergencyFundScore,
                "goalProgressScore", goalProgressScore
        ));
        return result;
    }

    // Higher savings ratio (income - expenses) / income is better. Target: >= 30% = 100.
    private double scoreSavingsRatio(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) return 0;
        double savingsRatio = income.subtract(expenses).divide(income, 4, RoundingMode.HALF_UP).doubleValue();
        return clampToScore(savingsRatio / 0.30);
    }

    // Lower expense ratio (expenses / income) is better. Target: <= 50% = 100.
    private double scoreExpenseRatio(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) return 0;
        double expenseRatio = expenses.divide(income, 4, RoundingMode.HALF_UP).doubleValue();
        return clampToScore((1 - expenseRatio) / 0.50);
    }

    // Higher investment-to-income ratio is better. Target: total investments >= 12x monthly income = 100.
    private double scoreInvestmentRatio(BigDecimal monthlyIncome, BigDecimal totalInvestments) {
        if (monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) return 0;
        double ratio = totalInvestments.divide(monthlyIncome, 4, RoundingMode.HALF_UP).doubleValue();
        return clampToScore(ratio / 12.0);
    }

    // Lower debt (EMI) burden relative to income is better. Target: EMI <= 20% of income = 100.
    private double scoreDebtRatio(BigDecimal income, BigDecimal loanEmi) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) return loanEmi.compareTo(BigDecimal.ZERO) > 0 ? 0 : 100;
        double debtRatio = loanEmi.divide(income, 4, RoundingMode.HALF_UP).doubleValue();
        return clampToScore((1 - debtRatio) / 0.80);
    }

    // Emergency fund should cover 6 months of expenses = 100.
    private double scoreEmergencyFund(BigDecimal balance, BigDecimal monthlyExpenses) {
        if (monthlyExpenses.compareTo(BigDecimal.ZERO) <= 0) return 100;
        double monthsCovered = balance.divide(monthlyExpenses, 4, RoundingMode.HALF_UP).doubleValue();
        return clampToScore(monthsCovered / 6.0);
    }

    // Average progress across active goals.
    private double scoreGoalProgress(List<Goal> goals) {
        if (goals.isEmpty()) return 50; // neutral score when no goals set
        double avg = goals.stream()
                .mapToDouble(g -> {
                    if (g.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) return 0;
                    return g.getCurrentAmount().divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
                })
                .average()
                .orElse(0);
        return Math.min(100, avg);
    }

    private double clampToScore(double ratioOfTarget) {
        return Math.max(0, Math.min(100, ratioOfTarget * 100));
    }

    private List<String> buildSuggestions(double savings, double expense, double investment,
                                           double debt, double emergencyFund, double goalProgress) {
        List<String> suggestions = new ArrayList<>();

        if (savings < 60) suggestions.add("Increase your monthly savings rate");
        if (expense < 60) suggestions.add("Reduce discretionary spending, e.g. Shopping and Entertainment");
        if (investment < 60) suggestions.add("Increase SIP contributions to grow your investments");
        if (debt < 60) suggestions.add("Reduce loan EMI burden relative to income where possible");
        if (emergencyFund < 60) suggestions.add("Maintain an emergency fund covering at least 6 months of expenses");
        if (goalProgress < 60) suggestions.add("Increase monthly contributions toward your financial goals");

        if (suggestions.isEmpty()) {
            suggestions.add("Great job! Keep maintaining your current financial habits");
        }

        return suggestions;
    }
}
