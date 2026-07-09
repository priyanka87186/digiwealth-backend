package com.digiwealth.ai.dto.request;

import com.digiwealth.ai.entity.Investment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestmentRequest {

    @NotNull
    private Investment.InvestmentType investmentType;

    @NotNull
    private BigDecimal investedAmount;

    @NotNull
    private BigDecimal currentValue;
}
