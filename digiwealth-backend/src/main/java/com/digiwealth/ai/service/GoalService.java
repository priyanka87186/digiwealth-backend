package com.digiwealth.ai.service;

import com.digiwealth.ai.dto.request.GoalRequest;
import com.digiwealth.ai.dto.response.GoalResponse;

import java.util.List;

public interface GoalService {
    GoalResponse createGoal(Long userId, GoalRequest request);
    List<GoalResponse> getGoals(Long userId);
    GoalResponse updateGoal(Long userId, Long goalId, GoalRequest request);
}
