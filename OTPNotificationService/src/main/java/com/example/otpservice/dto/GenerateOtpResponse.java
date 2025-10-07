package com.example.otpservice.dto;
import lombok.Data;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateOtpResponse {
    private String otpId;
    private OffsetDateTime expiresAt;
}


