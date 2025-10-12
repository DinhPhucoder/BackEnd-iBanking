package com.example.paymentservice.controller;

import com.example.paymentservice.dto.*;
import com.example.paymentservice.service.PaymentOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private PaymentOrchestratorService orchestratorService;

    public PaymentController(PaymentOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @GetMapping("/transaction-id")
    public ResponseEntity<TransactionIdResponse> getTransactionId() {
        String id = orchestratorService.getTransactionId();
        return ResponseEntity.ok(TransactionIdResponse.builder().transactionId(id).build());
    }

    @PostMapping
    public ResponseEntity<?> initiate(@RequestBody PaymentInitRequest request) {
        PaymentInitResponse res = orchestratorService.initiate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody PaymentConfirmRequest request) {
        PaymentConfirmResponse res = orchestratorService.confirm(request);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String transactionId) {
        // Khi orchestratorService.cancel ném ra ResponseStatusException,
        // RestExceptionHandler sẽ tự động bắt và xử lý nó.
        PaymentCancelResponse res = orchestratorService.cancel(transactionId);
        return ResponseEntity.ok(res);
    }
}
