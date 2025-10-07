package com.example.paymentservice.dto;
import lombok.*;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequest {
    private String transactionId;
    private BigInteger otpId;
    private String otpCode;
}