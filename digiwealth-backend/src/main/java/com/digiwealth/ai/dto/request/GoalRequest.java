package com.digiwealth.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {

    @NotBlank
    private String goalName;

    @NotNull
    private BigDecimal targetAmount;

    @NotNull
    private LocalDate targetDate;

    private BigDecimal monthlyContribution;

    private BigDecimal currentAmount;
}
