package com.example.tuitionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusRequest {
    private String transactionId;
    private BigDecimal amount;
}
