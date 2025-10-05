package com.example.paymentservice.dto;
import lombok.*;
import java.math.BigInteger;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitRequest {
    private BigInteger userId;
    private String mssv;
    private BigDecimal amount;
}
