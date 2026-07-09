package com.digiwealth.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestmentType investmentType;

    @Column(nullable = false)
    private BigDecimal investedAmount;

    @Column(nullable = false)
    private BigDecimal currentValue;

    public enum InvestmentType {
        MUTUAL_FUNDS, STOCKS, FIXED_DEPOSITS, BONDS, GOLD, NPS
    }
}
