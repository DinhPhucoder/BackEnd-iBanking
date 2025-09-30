package com.example.paymentservice.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmResponse {
    private String status;         // ví dụ: "SUCCESS" | "FAILED"
    private String transactionId;  // UUID khi thành công
}