package com.digiwealth.ai.service;

import com.digiwealth.ai.dto.request.TransactionRequest;
import com.digiwealth.ai.dto.response.TransactionResponse;
import com.digiwealth.ai.entity.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    TransactionResponse addTransaction(Long userId, TransactionRequest request);
    List<TransactionResponse> getTransactions(Long userId);
    List<TransactionResponse> filterByDate(Long userId, LocalDate start, LocalDate end);
    List<TransactionResponse> filterByCategory(Long userId, Transaction.Category category);
    List<TransactionResponse> search(Long userId, String keyword);
    Map<String, Object> getMonthlySummary(Long userId);
}
