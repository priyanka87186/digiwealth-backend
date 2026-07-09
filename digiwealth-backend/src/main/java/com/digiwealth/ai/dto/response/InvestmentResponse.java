package com.digiwealth.ai.dto.response;

import com.digiwealth.ai.entity.Investment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentResponse {
    private Long id;
    private Investment.InvestmentType investmentType;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private double allocationPercentage;
}
