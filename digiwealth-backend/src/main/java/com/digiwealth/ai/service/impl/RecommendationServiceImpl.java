package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.entity.Recommendation;
import com.digiwealth.ai.entity.User;
import com.digiwealth.ai.exception.ResourceNotFoundException;
import com.digiwealth.ai.repository.RecommendationRepository;
import com.digiwealth.ai.repository.UserRepository;
import com.digiwealth.ai.service.CustomerService;
import com.digiwealth.ai.service.FinancialHealthService;
import com.digiwealth.ai.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final FinancialHealthService financialHealthService;
    private final CustomerService customerService;

    @Override
    @SuppressWarnings("unchecked")
    public List<Recommendation> generateRecommendations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        var health = financialHealthService.calculateHealthScore(userId);
        List<String> suggestions = (List<String>) health.get("suggestions");

        return suggestions.stream()
                .map(text -> recommendationRepository.save(Recommendation.builder()
                        .user(user)
                        .recommendationType("FINANCIAL_HEALTH")
                        .recommendationText(text)
                        .priority(Recommendation.Priority.MEDIUM)
                        .build()))
                .toList();
    }

    @Override
    public List<Recommendation> getRecommendations(Long userId) {
        return recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
