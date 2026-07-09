package com.digiwealth.ai.repository;

import com.digiwealth.ai.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByDateDesc(Long accountId);

    List<Transaction> findByAccountIdAndDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByAccountIdAndCategory(Long accountId, Transaction.Category category);

    List<Transaction> findByAccountIdAndDescriptionContainingIgnoreCase(Long accountId, String keyword);
}
