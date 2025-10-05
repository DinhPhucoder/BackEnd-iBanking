package com.example.otpservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OTPController {

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestBody Map<String, Object> request) {
        // Mock: Luôn tạo OTP thành công
        Long userId = Long.valueOf(request.get("userId").toString());
        Long transactionId = Long.valueOf(request.get("transactionId").toString());
        
        Map<String, Object> response = Map.of(
            "otpId", System.currentTimeMillis(),
            "message", "OTP sent successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, Object> request) {
        // Mock: Luôn verify thành công
        Map<String, Object> response = Map.of("valid", true);
        return ResponseEntity.ok(response);
    }
}
