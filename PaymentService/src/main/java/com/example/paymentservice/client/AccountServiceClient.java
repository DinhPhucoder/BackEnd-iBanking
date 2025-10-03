package com.example.paymentservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccountServiceClient {
    private RestTemplate restTemplate;
    private String accountBaseUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                                @Qualifier("accountBaseUrl") String accountBaseUrl) {
        this.restTemplate = restTemplate;
        this.accountBaseUrl = accountBaseUrl;
    }

    public Boolean lockUser(Long userId) {
        String url = accountBaseUrl + "/accounts/{userId}/lock";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("transactionId", System.currentTimeMillis()); // Generate transactionId
        try {
            ResponseEntity<LockResponse> res = restTemplate.postForEntity(url, body, LockResponse.class, userId);
            return res.getBody() != null && Boolean.TRUE.equals(res.getBody().getLocked());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                return false; // đã bị khóa -> báo false để orchestrator trả 409 hợp lệ
            }
            throw e;
        }
    }

    public Boolean unlockUser(Long userId) {
        String url = accountBaseUrl + "/accounts/{userId}/unlock";
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("lockKey", "lock_" + userId + "_" + System.currentTimeMillis());
        body.put("transactionId", System.currentTimeMillis());
        ResponseEntity<UnlockResponse> res = restTemplate.postForEntity(url, body, UnlockResponse.class, userId);
        return res.getBody() != null && Boolean.TRUE.equals(res.getBody().getUnlocked());
    }

    public Long getBalance(Long userId) {
        String url = accountBaseUrl + "/accounts/{userId}/balance";
        return restTemplate.getForObject(url, Long.class, userId);
    }
    
    public Boolean getAccount(Long userId) {
        String url = accountBaseUrl + "/accounts/{userId}/balance";
        try {
            restTemplate.getForObject(url, Long.class, userId);
            return true; // Account exists
        } catch (Exception ex) {
            return false; // Account not found
        }
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
        ResponseEntity<BalanceResponse> res = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(body),
                BalanceResponse.class,
                userId
        );
        return res.getStatusCode().is2xxSuccessful() && res.getBody() != null && res.getBody().getNewBalance() != null;
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

    // Simple DTOs to deserialize AccountService responses
    public static class LockResponse {
        private Boolean locked;
        private String lockKey;
        private Long expiry;
        public Boolean getLocked() { return locked; }
        public void setLocked(Boolean locked) { this.locked = locked; }
        public String getLockKey() { return lockKey; }
        public void setLockKey(String lockKey) { this.lockKey = lockKey; }
        public Long getExpiry() { return expiry; }
        public void setExpiry(Long expiry) { this.expiry = expiry; }
    }

    public static class UnlockResponse {
        private Boolean unlocked;
        public Boolean getUnlocked() { return unlocked; }
        public void setUnlocked(Boolean unlocked) { this.unlocked = unlocked; }
    }

    public static class BalanceResponse {
        private Long userId;
        private Long newBalance;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getNewBalance() { return newBalance; }
        public void setNewBalance(Long newBalance) { this.newBalance = newBalance; }
    }
}
