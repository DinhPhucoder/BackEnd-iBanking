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
import java.math.BigInteger;

@RestController
@RequestMapping("/otp")
public class OTPController {

    private final OtpService otpService;
    private final EmailService emailService;

    public OTPController(OtpService otpService, EmailService emailService) {
        this.otpService = otpService;
        this.emailService = emailService;
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
        if (userId == null || userId.compareTo(BigInteger.ZERO) <= 0 || type == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid input or missing required fields for type", 400));
        }

        // Validate theo type
        if ("OTP".equals(type)) {
            if (request.getOtpCode() == null || !request.getOtpCode().matches("\\d{6}")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Invalid input or missing required fields for type", 400));
            }
        } else if ("CONFIRMATION".equals(type)) {
            if (request.getTransactionId() == null || request.getTransactionId().isBlank()
                    || request.getMssv() == null || request.getMssv().isBlank()
                    || request.getAmount() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Invalid input or missing required fields for type", 400));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid input or missing required fields for type", 400));
        }

        // lookup email user
        String email = emailService.getUserEmail(userId);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User or email not found", 404));
        }

        String subject;
        String html;
        if ("OTP".equals(type)) {
            subject = "iBanking OTP";
            html = "<h2>iBanking OTP</h2>\n"
                    + "<p>Your OTP is <strong>" + request.getOtpCode() + "</strong>. It is valid for 1 minutes.</p>\n"
                    + "<p>Please do not share this OTP with anyone.</p>\n"
                    + "<p>Thank you for using iBanking!</p>";
        } else {
            subject = "Transaction Confirmation";
            html = "<h2>Transaction Confirmation</h2>\n"
                    + "<p>You have successfully paid <strong>" + request.getAmount() + " VND</strong> for MSSV <strong>" + request.getMssv() + "</strong>.</p>\n"
                    + "<p>Transaction ID: <strong>" + request.getTransactionId() + "</strong></p>\n"
                    + "<p>Thank you for using iBanking!</p>";
        }

        boolean sent = emailService.sendHtml(email, subject, html);
        if (!sent) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to send email", 500));
        }
        return ResponseEntity.ok(java.util.Map.of("sent", true, "message", "Email sent successfully"));
    }
    @GetMapping("/test-email/{userId}")
    public ResponseEntity<?> testGetEmail(@PathVariable BigInteger userId) {
        String email = emailService.getUserEmail(userId);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User or email not found", 404));
        }
        return ResponseEntity.ok(java.util.Map.of("email", email));
    }

}
