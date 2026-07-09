package com.digiwealth.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private LocalDateTime date;

    private String description;

    public enum Category {
        FOOD, TRAVEL, ENTERTAINMENT, SHOPPING, BILLS, HEALTHCARE, EDUCATION, INVESTMENTS, SALARY, LOAN_EMI, OTHERS
    }

    public enum TransactionType {
        CREDIT, DEBIT
    }
}
