package com.example.otpservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateOtpRequest {
    @NotNull(message = "userId is required")
    private BigInteger userId;
    @NotNull(message = "transactionId is required")
    private String transactionId;
}


