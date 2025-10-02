package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentConfirmRequest;
import com.example.paymentservice.dto.PaymentConfirmResponse;
import com.example.paymentservice.dto.PaymentInitRequest;
import com.example.paymentservice.dto.PaymentInitResponse;
import com.example.paymentservice.service.PaymentOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentOrchestratorService orchestratorService;

    public PaymentController(PaymentOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping
    public ResponseEntity<PaymentInitResponse> initiate(@RequestBody PaymentInitRequest request) {
        PaymentInitResponse res = orchestratorService.initiate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(@RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(orchestratorService.confirm(request));
    }
}


