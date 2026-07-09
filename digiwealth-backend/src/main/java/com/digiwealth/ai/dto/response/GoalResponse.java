package com.digiwealth.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private BigDecimal currentAmount;
    private BigDecimal monthlyContribution;
    private double progressPercentage;
}
