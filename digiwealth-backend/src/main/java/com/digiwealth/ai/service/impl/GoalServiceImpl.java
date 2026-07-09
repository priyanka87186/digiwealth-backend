package com.digiwealth.ai.service.impl;

import com.digiwealth.ai.dto.request.GoalRequest;
import com.digiwealth.ai.dto.response.GoalResponse;
import com.digiwealth.ai.entity.Goal;
import com.digiwealth.ai.entity.User;
import com.digiwealth.ai.exception.BadRequestException;
import com.digiwealth.ai.exception.ResourceNotFoundException;
import com.digiwealth.ai.repository.GoalRepository;
import com.digiwealth.ai.repository.UserRepository;
import com.digiwealth.ai.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    @Override
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Goal goal = Goal.builder()
                .user(user)
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO)
                .monthlyContribution(request.getMonthlyContribution())
                .build();

        return toResponse(goalRepository.save(goal));
    }

    @Override
    public List<GoalResponse> getGoals(Long userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GoalResponse updateGoal(Long userId, Long goalId, GoalRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        if (!goal.getUser().getId().equals(userId)) {
            throw new BadRequestException("Goal does not belong to the authenticated user");
        }

        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount());
        }
        goal.setMonthlyContribution(request.getMonthlyContribution());

        return toResponse(goalRepository.save(goal));
    }

    private GoalResponse toResponse(Goal goal) {
        double progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount()
                        .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                : 0.0;

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .currentAmount(goal.getCurrentAmount())
                .monthlyContribution(goal.getMonthlyContribution())
                .progressPercentage(Math.min(progress, 100.0))
                .build();
    }
}
