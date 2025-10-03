package com.example.paymentservice.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmResponse {
    private String status;         // "success" | "failed"
    private String transactionId;  // String khi thành công
}