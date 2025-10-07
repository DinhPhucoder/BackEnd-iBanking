package com.example.otpservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @NotNull(message = "userId is required")
    private BigInteger userId;

    @NotNull(message = "type is required")
    private String type;

    private String otpId;            // required if type=OTP
    
    private String transactionId;      // required if type=CONFIRMATION
    private java.math.BigDecimal amount; // required if type=CONFIRMATION
    private String mssv;               // required if type=CONFIRMATION
}


