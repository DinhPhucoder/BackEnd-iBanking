package com.example.otpservice.controller;

import com.example.otpservice.dto.ErrorResponse;
import com.example.otpservice.dto.GenerateOtpRequest;
import com.example.otpservice.dto.GenerateOtpResponse;
import com.example.otpservice.service.OtpService;
import com.example.otpservice.dto.EmailRequest;
import com.example.otpservice.service.EmailService;
import com.example.otpservice.dto.VerifyOtpRequest;
import com.example.otpservice.service.OtpService.RateLimitExceededException;
import jakarta.validation.Valid;
import com.example.otpservice.service.OtpService.VerifyResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OTPController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final OtpService otpService;
    private final EmailService emailService;

    public OTPController(OtpService otpService, EmailService emailService) {
        this.otpService = otpService;
        this.emailService = emailService;
    }

    private String formatAmount(BigDecimal amount) {
        Locale vietnam = new Locale("vi", "VN");
        NumberFormat formatterVND = NumberFormat.getCurrencyInstance(vietnam);
        return formatterVND.format(amount).replace("₫", "VND");
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
        BigInteger userId = request.getUserId();
        String transactionId = request.getTransactionId();

        if (userId == null || transactionId == null || userId.compareTo(BigInteger.ZERO) <= 0 || transactionId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid userId or transactionId", 400));
        }

        try {
            GenerateOtpResponse resp = otpService.generate(userId, transactionId);
            return ResponseEntity.ok(resp);
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse("Too many OTP requests for this transaction", 429));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyResult result = otpService.verify(request.getOtpId(), request.getOtpCode());
        switch (result) {
            case INVALID_INPUT:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Invalid otpId or otpCode", 400));
            case EXPIRED_OR_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("OTP not found", 404));
            case INVALID_CODE:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid OTP", 401));
            case VALID:
                return ResponseEntity.ok(java.util.Map.of("valid", true));
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Internal error", 500));
        }
    }

    @PostMapping("/notifications/email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailRequest request) {
        BigInteger userId = request.getUserId();
        String type = request.getType();

        if (userId.compareTo(BigInteger.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("userId must be positive", 400));
        }

        // Validate theo type
        if ("OTP".equals(type)) {
            if (request.getOtpId() == null || request.getOtpId().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Missing otpId", 400));
            }

            String key = "otp:" + request.getOtpId();
            Object stored = redisTemplate.opsForValue().get(key);

            if (stored == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("OTP not found or expired", 404));
            }
            String otpCode = stored.toString();
            String to = emailService.getUserEmail(userId);
            if (to == null || to.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User or email not found", 404));
            }
            String subject = "iBanking OTP Verification";
            String html = "<h2>iBanking OTP</h2>"
                    + "<p>Your OTP code is: <b>" + otpCode + "</b></p>"
                    + "<p>This code is valid for <b>1 minute</b>. Do not share it with anyone.</p>"
                    + "<p>Thank you for using iBanking!</p>";
            boolean sent = emailService.sendHtml(to, subject, html);
            if (!sent) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Failed to send email", 500));
            }

            return ResponseEntity.ok(Map.of("sent", true, "message", "OTP email sent successfully"));
        } else if ("CONFIRMATION".equals(type)) {
            if (request.getTransactionId() == null || request.getTransactionId().isBlank()
                    || request.getMssv() == null || request.getMssv().isBlank()
                    || request.getAmount() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Invalid input or missing required fields for type", 400));
            }
            String to = emailService.getUserEmail(userId);
            if (to == null || to.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User or email not found", 404));
            }

            String subject = "iBanking Payment Confirmation";
            String formattedAmount = formatAmount(request.getAmount());
            String html = "<h2>Payment Confirmation</h2>"
                    + "<p>Your payment has been successfully processed.</p>"
                    + "<ul>"
                    + "<li><b>MSSV:</b> " + request.getMssv() + "</li>"
                    + "<li><b>Transaction ID:</b> " + request.getTransactionId() + "</li>"
                    + "<li><b>Amount:</b> " + formattedAmount + "</li>"
                    + "</ul>"
                    + "<p>Thank you for using iBanking!</p>";

            boolean sent = emailService.sendHtml(to, subject, html);
            if (!sent) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Failed to send email", 500));
            }
            return ResponseEntity.ok(Map.of("sent", true, "message", "Confirmation email sent successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid input or missing required fields for type", 400));
        }
    }
}
