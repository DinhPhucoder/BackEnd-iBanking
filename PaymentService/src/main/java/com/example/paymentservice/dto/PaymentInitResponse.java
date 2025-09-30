package com.example.paymentservice.dto;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitResponse {
    private String paymentId;
    private boolean otpRequired;
}
