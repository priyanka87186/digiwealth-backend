package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.dto.request.InvestmentRequest;
import com.digiwealth.ai.dto.response.InvestmentResponse;
import com.digiwealth.ai.entity.Investment;
import com.digiwealth.ai.entity.User;
import com.digiwealth.ai.exception.ResourceNotFoundException;
import com.digiwealth.ai.repository.InvestmentRepository;
import com.digiwealth.ai.repository.UserRepository;
import com.digiwealth.ai.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    @Override
    public InvestmentResponse addInvestment(Long userId, InvestmentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Investment investment = Investment.builder()
                .user(user)
                .investmentType(request.getInvestmentType())
                .investedAmount(request.getInvestedAmount())
                .currentValue(request.getCurrentValue())
                .build();

        investment = investmentRepository.save(investment);

        List<Investment> all = investmentRepository.findByUserId(userId);
        BigDecimal total = all.stream().map(Investment::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return toResponse(investment, total);
    }

    @Override
    public List<InvestmentResponse> getPortfolio(Long userId) {
        List<Investment> investments = investmentRepository.findByUserId(userId);
        BigDecimal total = investments.stream().map(Investment::getCurrentValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return investments.stream()
                .map(inv -> toResponse(inv, total))
                .collect(Collectors.toList());
    }

    private InvestmentResponse toResponse(Investment investment, BigDecimal portfolioTotal) {
        BigDecimal profitLoss = investment.getCurrentValue().subtract(investment.getInvestedAmount());

        double allocation = portfolioTotal.compareTo(BigDecimal.ZERO) > 0
                ? investment.getCurrentValue()
                        .divide(portfolioTotal, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                : 0.0;

        return InvestmentResponse.builder()
                .id(investment.getId())
                .investmentType(investment.getInvestmentType())
                .investedAmount(investment.getInvestedAmount())
                .currentValue(investment.getCurrentValue())
                .profitLoss(profitLoss)
                .allocationPercentage(allocation)
                .build();
    }
}
