package com.digiwealth.ai.repository;

import com.digiwealth.ai.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
