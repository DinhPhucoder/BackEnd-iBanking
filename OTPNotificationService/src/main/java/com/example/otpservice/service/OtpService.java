package com.example.otpservice.service;

import java.math.BigInteger;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.otpservice.dto.GenerateOtpResponse;

@Service
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final String userBaseUrl;
    private final String accountBaseUrl;
    private final String paymentBaseUrl;

    public OtpService(RedisTemplate<String, Object> redisTemplate,
                      RestTemplate restTemplate,
                      @Value("${services.user.base-url}") String userBaseUrl,
                      @Value("${services.account.base-url}") String accountBaseUrl,
                      @Value("${services.payment.base-url}") String paymentBaseUrl) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
        this.userBaseUrl = userBaseUrl;
        this.accountBaseUrl = accountBaseUrl;
        this.paymentBaseUrl = paymentBaseUrl;
    }

    public boolean userExists(BigInteger userId) {
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(userBaseUrl + "/users/" + userId, String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public GenerateOtpResponse generate(BigInteger userId, String transactionId) {
        // bien dem request
        String rateKey = "rate:otp:" + transactionId;
        Long count = redisTemplate.opsForValue().increment(rateKey); //++1 count
        if (count != null && count == 1L) {
            redisTemplate.expire(rateKey, java.time.Duration.ofMinutes(4));
        }
        if (count != null && count > 3) {
            throw new RateLimitExceededException();
        }

        String otpCode = generateOtpCode();
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(60);
        String otpId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("otp:" + otpId, otpCode, Duration.ofSeconds(60));

        com.example.otpservice.dto.GenerateOtpResponse res = new com.example.otpservice.dto.GenerateOtpResponse();
        res.setOtpId(otpId);
        res.setExpiresAt(expiresAt);
        return res;
    }
    
    public VerifyResult verify(String otpId, String otpCode) {
        if (otpId == null || otpId.isBlank() || otpCode == null || !otpCode.matches("\\d{6}")) {
            return VerifyResult.INVALID_INPUT;
        }

        String key = "otp:" + otpId;
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            return VerifyResult.EXPIRED_OR_NOT_FOUND;
        }

        if (!stored.toString().equals(otpCode)) {
            return VerifyResult.INVALID_CODE;
        }

        // Xoá OTP để đảm bảo one-time
        redisTemplate.delete(key);

        return VerifyResult.VALID;
    }

    private String generateOtpCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // Kết quả verify
    public enum VerifyResult {
        VALID,
        INVALID_INPUT,
        EXPIRED_OR_NOT_FOUND,
        INVALID_CODE
    }

    public static class RateLimitExceededException extends RuntimeException {}
}



