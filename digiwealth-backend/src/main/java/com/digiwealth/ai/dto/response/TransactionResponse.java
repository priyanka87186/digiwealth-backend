package com.digiwealth.ai.dto.response;

import com.digiwealth.ai.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private Transaction.Category category;
    private Transaction.TransactionType transactionType;
    private LocalDateTime date;
    private String description;
}
