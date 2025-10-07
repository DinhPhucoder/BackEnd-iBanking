package com.example.paymentservice.dto;
import lombok.*;

import java.math.BigInteger;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitResponse {
    private String transactionId;
    private BigInteger otpId;
}
