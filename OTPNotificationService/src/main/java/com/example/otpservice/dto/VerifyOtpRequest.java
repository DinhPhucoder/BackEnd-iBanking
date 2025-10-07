package com.example.otpservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpRequest {
    @NotBlank(message = "otpId is required")
    private String otpId;

    @NotBlank(message = "otpCode is required")
    @Pattern(regexp = "\\d{6}", message = "otpCode must be 6 digits")
    private String otpCode;
}


