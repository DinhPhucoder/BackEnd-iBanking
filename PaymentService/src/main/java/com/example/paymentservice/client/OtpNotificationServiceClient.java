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


    // Generate OTP: trả về otpId theo đặc tả
    public BigInteger generateOtp(BigInteger userId, String transactionId){
        String url = otpBaseUrl + "/otp/generate";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("transactionId", transactionId);
        ResponseEntity<String> res = restTemplate.postForEntity(url, body, String.class);
        try {
            return new BigInteger(res.getBody()); // Convert string to BigInteger
        } catch (NumberFormatException ex) {
            return BigInteger.valueOf(System.currentTimeMillis()); // Fallback to timestamp
        }
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
        }
    }

    public static class ValidResponse {
        private Boolean valid;
        public Boolean getValid() { return valid; }
        public void setValid(Boolean valid) { this.valid = valid; }
    }
}
