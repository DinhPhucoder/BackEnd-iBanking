package com.example.paymentservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Component
public class OtpNotificationServiceClient {
    private RestTemplate restTemplate;
    private String otpBaseUrl;
    public  OtpNotificationServiceClient(RestTemplate restTemplate,
                                         @Qualifier("otpBaseUrl") String otpBaseUrl) {
        this.restTemplate = restTemplate;
        this.otpBaseUrl = otpBaseUrl;
    }


    // Generate OTP: trả về otpId và expiresAt theo đặc tả
    public GenerateResponse generateOtp(BigInteger userId, String transactionId){
        String url = otpBaseUrl + "/otp/generate";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("transactionId", transactionId);
        ResponseEntity<GenerateResponse> res = restTemplate.postForEntity(url, body, GenerateResponse.class);
        return res.getBody();
    }

    // Verify OTP: gửi { otpId, otpCode } – chấp nhận cả {"valid":true} hoặc boolean true/false
    public Boolean verifyOtp(String otpId, String otpCode){
        String url = otpBaseUrl + "/otp/verify";
        Map<String, Object> body = new HashMap<>();
        body.put("otpId", otpId);
        body.put("otpCode", otpCode);
        try {
            ResponseEntity<ValidResponse> res = restTemplate.postForEntity(url, body, ValidResponse.class);
            return res.getBody() != null && Boolean.TRUE.equals(res.getBody().getValid());
        } catch (org.springframework.http.converter.HttpMessageNotReadableException ex) {
            ResponseEntity<Boolean> res = restTemplate.postForEntity(url, body, Boolean.class);
            return res.getBody() != null && res.getBody();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Map status codes theo mô tả
            if (e.getStatusCode().value() == 401) return false; // Invalid OTP
            if (e.getStatusCode().value() == 410) return false; // OTP expired
            if (e.getStatusCode().value() == 404) return false; // OTP not found
            if (e.getStatusCode().value() == 429) return false; // Too Many Requests
            throw e;
        }
    }

    public static class ValidResponse {
        private Boolean valid;
        public Boolean getValid() { return valid; }
        public void setValid(Boolean valid) { this.valid = valid; }
    }

    public static class GenerateResponse {
        private String otpId; // OTP service trả String
        private String expiresAt;
        public String getOtpId() { return otpId; }
        public void setOtpId(String otpId) { this.otpId = otpId; }
        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    }

    // Gửi email: type = OTP hoặc CONFIRMATION
    public void sendEmailOtp(java.math.BigInteger userId, String otpCode, java.math.BigInteger transactionId) {
        String url = otpBaseUrl + "/notifications/email";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("type", "OTP");
        body.put("otpCode", otpCode);
        body.put("transactionId", transactionId);
        try { restTemplate.postForEntity(url, body, Map.class); } catch (Exception ignore) {}
    }

    public void sendEmailConfirmation(java.math.BigInteger userId, java.math.BigInteger transactionId, java.math.BigDecimal amount, String mssv) {
        String url = otpBaseUrl + "/notifications/email";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("type", "CONFIRMATION");
        body.put("transactionId", transactionId);
        body.put("amount", amount);
        body.put("mssv", mssv);
        try { restTemplate.postForEntity(url, body, Map.class); } catch (Exception ignore) {}
    }
}
