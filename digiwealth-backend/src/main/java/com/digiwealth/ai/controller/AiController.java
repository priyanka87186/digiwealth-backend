package com.digiwealth.ai.controller;

import com.digiwealth.ai.dto.request.AiChatRequest;
import com.digiwealth.ai.dto.response.AiChatResponse;
import com.digiwealth.ai.entity.Recommendation;
import com.digiwealth.ai.security.UserPrincipal;
import com.digiwealth.ai.service.AiService;
import com.digiwealth.ai.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final RecommendationService recommendationService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiService.chat(principal.getId(), request));
    }

    @PostMapping("/recommendation")
    public ResponseEntity<List<Recommendation>> recommendation(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recommendationService.generateRecommendations(principal.getId()));
    }
}
