package com.example.paymentservice.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitRequest {
    private Long userId;
    private String mssv;
    private Long amount;
}
