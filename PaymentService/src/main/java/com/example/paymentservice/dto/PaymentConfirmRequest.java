package com.example.paymentservice.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequest {
    private String transactionId;
    private String otpId;
    private String otpCode;
}