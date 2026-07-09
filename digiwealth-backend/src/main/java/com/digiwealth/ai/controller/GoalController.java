package com.digiwealth.ai.controller;

import com.digiwealth.ai.dto.request.GoalRequest;
import com.digiwealth.ai.dto.response.GoalResponse;
import com.digiwealth.ai.security.UserPrincipal;
import com.digiwealth.ai.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(goalService.getGoals(principal.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable("id") Long goalId,
                                                @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(principal.getId(), goalId, request));
    }
}
