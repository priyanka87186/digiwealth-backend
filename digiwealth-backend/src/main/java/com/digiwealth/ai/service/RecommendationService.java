package com.digiwealth.ai.service;

import com.digiwealth.ai.entity.Recommendation;

import java.util.List;

public interface RecommendationService {
    List<Recommendation> generateRecommendations(Long userId);
    List<Recommendation> getRecommendations(Long userId);
}
