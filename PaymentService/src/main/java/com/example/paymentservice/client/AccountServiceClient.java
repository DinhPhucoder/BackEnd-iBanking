package com.example.paymentservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccountServiceClient {
    private final RestTemplate restTemplate;
    private final String accountBaseUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                                @Qualifier("accountBaseUrl") String accountBaseUrl) {
        this.restTemplate = restTemplate;
        this.accountBaseUrl = accountBaseUrl;
    }

    public Boolean lockUser(Long userId) {
        String url = accountBaseUrl + "/accounts/{userId}/lock";
        ResponseEntity<Boolean> res = restTemplate.postForEntity(url, null, Boolean.class, userId);
        return res.getBody() != null && res.getBody();
    }

    public Boolean unlockUser(Long userId) {
        String url = accountBaseUrl + "/accounts/{userId}/unlock";
        ResponseEntity<Boolean> res = restTemplate.postForEntity(url, null, Boolean.class, userId);
        return res.getBody() != null && res.getBody();
    }

    public Long getBalance(Long userId) {
        String url = accountBaseUrl + "/accounts/{userId}/balance";
        return restTemplate.getForObject(url, Long.class, userId);
    }
    public Boolean checkBalance(Long userId, Long amount) {
        String url = accountBaseUrl + "/accounts/checkBalance";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", userId);
        requestBody.put("amount", amount);
        ResponseEntity<Boolean> res = restTemplate.postForEntity(url, requestBody, Boolean.class);
        return  res.getBody() != null && res.getBody();
    }

    public Boolean updateBalance(Long userId, Long amount, String transactionId) {
        String url = accountBaseUrl + "/accounts/{userId}/balance";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("amount", amount);
        body.put("transactionId", transactionId);
        ResponseEntity<Boolean> res = restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(body), Boolean.class, userId);
        return res.getBody() != null && res.getBody();
    }

    public String saveTransaction(Long userId, String transactionId, Long amount) {
        String url = accountBaseUrl + "/transactions";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("type", "TUITION_PAYMENT");
        body.put("amount", amount);
        body.put("description", "Tuition payment transaction " + transactionId);
        body.put("transactionId", transactionId);
        ResponseEntity<String> res = restTemplate.postForEntity(url, body, String.class);
        return res.getBody();
    }
}
