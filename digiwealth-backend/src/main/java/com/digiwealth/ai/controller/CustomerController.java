package com.digiwealth.ai.controller;

import com.digiwealth.ai.dto.response.DashboardResponse;
import com.digiwealth.ai.entity.User;
import com.digiwealth.ai.security.UserPrincipal;
import com.digiwealth.ai.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.getProfile(principal.getId()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(customerService.getDashboard(principal.getId()));
    }
}
