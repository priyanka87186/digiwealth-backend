package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.dto.response.DashboardResponse;
import com.digiwealth.ai.entity.Account;
import com.digiwealth.ai.entity.Goal;
import com.digiwealth.ai.entity.Investment;
import com.digiwealth.ai.entity.Transaction;
import com.digiwealth.ai.entity.User;
import com.digiwealth.ai.exception.ResourceNotFoundException;
import com.digiwealth.ai.repository.AccountRepository;
import com.digiwealth.ai.repository.GoalRepository;
import com.digiwealth.ai.repository.InvestmentRepository;
import com.digiwealth.ai.repository.TransactionRepository;
import com.digiwealth.ai.repository.UserRepository;
import com.digiwealth.ai.service.CustomerService;
import com.digiwealth.ai.service.FinancialHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final InvestmentRepository investmentRepository;
    private final GoalRepository goalRepository;
    private final FinancialHealthService financialHealthService;

    @Override
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public DashboardResponse getDashboard(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpenses = BigDecimal.ZERO;

        for (Account account : accounts) {
            List<Transaction> monthTx = transactionRepository.findByAccountIdAndDateBetween(
                    account.getId(), monthStart, monthEnd);

            for (Transaction tx : monthTx) {
                if (tx.getTransactionType() == Transaction.TransactionType.CREDIT) {
                    monthlyIncome = monthlyIncome.add(tx.getAmount());
                } else {
                    monthlyExpenses = monthlyExpenses.add(tx.getAmount());
                }
            }
        }

        BigDecimal savings = monthlyIncome.subtract(monthlyExpenses).max(BigDecimal.ZERO);

        List<Investment> investments = investmentRepository.findByUserId(userId);
        BigDecimal totalInvestments = investments.stream()
                .map(Investment::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Goal> goals = goalRepository.findByUserId(userId);
        long activeGoals = goals.stream()
                .filter(g -> g.getCurrentAmount().compareTo(g.getTargetAmount()) < 0)
                .count();

        Map<String, Object> health = financialHealthService.calculateHealthScore(userId);

        return DashboardResponse.builder()
                .totalBalance(totalBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .totalSavings(savings)
                .totalInvestments(totalInvestments)
                .activeGoals((int) activeGoals)
                .financialHealthScore((int) health.get("score"))
                .healthScoreSuggestions((List<String>) health.get("suggestions"))
                .build();
    }
}
