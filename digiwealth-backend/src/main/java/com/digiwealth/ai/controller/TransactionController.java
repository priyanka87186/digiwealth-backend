package com.digiwealth.ai.controller;

import com.digiwealth.ai.dto.request.TransactionRequest;
import com.digiwealth.ai.dto.response.TransactionResponse;
import com.digiwealth.ai.entity.Transaction;
import com.digiwealth.ai.security.UserPrincipal;
import com.digiwealth.ai.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> add(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll(@AuthenticationPrincipal UserPrincipal principal,
                                                              @RequestParam(required = false) String category,
                                                              @RequestParam(required = false)
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                              @RequestParam(required = false)
                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                              @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(transactionService.search(principal.getId(), search));
        }
        if (category != null) {
            return ResponseEntity.ok(transactionService.filterByCategory(
                    principal.getId(), Transaction.Category.valueOf(category.toUpperCase())));
        }
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(transactionService.filterByDate(principal.getId(), startDate, endDate));
        }
        return ResponseEntity.ok(transactionService.getTransactions(principal.getId()));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, Object>> monthlySummary(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(transactionService.getMonthlySummary(principal.getId()));
    }
}
