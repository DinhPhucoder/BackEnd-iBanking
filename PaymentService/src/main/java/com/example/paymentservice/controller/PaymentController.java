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
        try {
            PaymentInitResponse res = orchestratorService.initiate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (ResponseStatusException ex) {
            ErrorResponse error = ErrorResponse.builder()
                    .error(ex.getReason())
                    .code(ex.getStatusCode().value())
                    .build();
            return ResponseEntity.status(ex.getStatusCode()).body(error);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody PaymentConfirmRequest request) {
        try {
            PaymentConfirmResponse res = orchestratorService.confirm(request);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException ex) {
            ErrorResponse error = ErrorResponse.builder()
                    .error(ex.getReason())
                    .code(ex.getStatusCode().value())
                    .build();
            return ResponseEntity.status(ex.getStatusCode()).body(error);
        }
    }
}


