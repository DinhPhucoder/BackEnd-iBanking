package com.example.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCancelResponse {
    private String transactionId;
    private String status;
}
