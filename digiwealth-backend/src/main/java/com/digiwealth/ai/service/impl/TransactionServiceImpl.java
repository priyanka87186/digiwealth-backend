package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.dto.request.TransactionRequest;
import com.digiwealth.ai.dto.response.TransactionResponse;
import com.digiwealth.ai.entity.Account;
import com.digiwealth.ai.entity.Transaction;
import com.digiwealth.ai.exception.BadRequestException;
import com.digiwealth.ai.exception.ResourceNotFoundException;
import com.digiwealth.ai.repository.AccountRepository;
import com.digiwealth.ai.repository.TransactionRepository;
import com.digiwealth.ai.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public TransactionResponse addTransaction(Long userId, TransactionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));

        if (!account.getUser().getId().equals(userId)) {
            throw new BadRequestException("Account does not belong to the authenticated user");
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(request.getAmount())
                .category(request.getCategory())
                .transactionType(request.getTransactionType())
                .date(request.getDate() != null ? request.getDate() : LocalDateTime.now())
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);

        // Update running account balance.
        BigDecimal delta = request.getTransactionType() == Transaction.TransactionType.CREDIT
                ? request.getAmount()
                : request.getAmount().negate();
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);

        return toResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getTransactions(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .flatMap(acc -> transactionRepository.findByAccountIdOrderByDateDesc(acc.getId()).stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> filterByDate(Long userId, LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        return accountRepository.findByUserId(userId).stream()
                .flatMap(acc -> transactionRepository.findByAccountIdAndDateBetween(acc.getId(), startDt, endDt).stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> filterByCategory(Long userId, Transaction.Category category) {
        return accountRepository.findByUserId(userId).stream()
                .flatMap(acc -> transactionRepository.findByAccountIdAndCategory(acc.getId(), category).stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> search(Long userId, String keyword) {
        return accountRepository.findByUserId(userId).stream()
                .flatMap(acc -> transactionRepository
                        .findByAccountIdAndDescriptionContainingIgnoreCase(acc.getId(), keyword).stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getMonthlySummary(Long userId) {
        List<TransactionResponse> all = getTransactions(userId);

        Map<String, BigDecimal> byCategory = new HashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (TransactionResponse tx : all) {
            if (tx.getTransactionType() == Transaction.TransactionType.CREDIT) {
                totalIncome = totalIncome.add(tx.getAmount());
            } else {
                totalExpense = totalExpense.add(tx.getAmount());
                byCategory.merge(tx.getCategory().name(), tx.getAmount(), BigDecimal::add);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("expenseByCategory", byCategory);
        summary.put("transactionCount", all.size());
        return summary;
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .category(t.getCategory())
                .transactionType(t.getTransactionType())
                .date(t.getDate())
                .description(t.getDescription())
                .build();
    }
}
