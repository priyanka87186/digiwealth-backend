package com.digiwealth.ai.service;

import com.digiwealth.ai.dto.request.InvestmentRequest;
import com.digiwealth.ai.dto.response.InvestmentResponse;

import java.util.List;

public interface InvestmentService {
    InvestmentResponse addInvestment(Long userId, InvestmentRequest request);
    List<InvestmentResponse> getPortfolio(Long userId);
}
