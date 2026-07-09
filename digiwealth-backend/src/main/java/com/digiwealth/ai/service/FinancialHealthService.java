package com.digiwealth.ai.service;

import java.util.Map;

public interface FinancialHealthService {
    /**
     * Returns a map containing "score" (0-100 int) and "suggestions" (List<String>).
     */
    Map<String, Object> calculateHealthScore(Long userId);
}
