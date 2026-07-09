package com.digiwealth.ai.dto.request;

import com.digiwealth.ai.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequest {

    @NotNull
    private Long accountId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Transaction.Category category;

    @NotNull
    private Transaction.TransactionType transactionType;

    private LocalDateTime date;

    private String description;
}
