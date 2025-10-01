package com.example.paymentservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    public String generateOtp(Long userId, String transactionId){
        String url = otpBaseUrl + "/otp/generate";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("transactionId", transactionId);
        ResponseEntity<String> res = restTemplate.postForEntity(url, body, String.class);
        return res.getBody(); // kỳ vọng res body chứa otpId (chuỗi)
    }

    // Verify OTP: gửi { otpId, otpCode } và nhận { valid } hoặc boolean
    public Boolean verifyOtp(String otpId, String otpCode){
        String url = otpBaseUrl + "/otp/verify";
        Map<String, Object> body = new HashMap<>();
        body.put("otpId", otpId);
        body.put("otpCode", otpCode);
        ResponseEntity<Boolean> res = restTemplate.postForEntity(url, body, Boolean.class);
        return res.getBody() != null && res.getBody();
    }
}
