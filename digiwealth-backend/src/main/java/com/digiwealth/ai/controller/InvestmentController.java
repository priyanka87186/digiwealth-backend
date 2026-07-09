package com.digiwealth.ai.controller;

import com.digiwealth.ai.dto.request.InvestmentRequest;
import com.digiwealth.ai.dto.response.InvestmentResponse;
import com.digiwealth.ai.security.UserPrincipal;
import com.digiwealth.ai.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<InvestmentResponse> add(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investmentService.addInvestment(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<InvestmentResponse>> getPortfolio(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(investmentService.getPortfolio(principal.getId()));
    }
}
