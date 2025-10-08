package com.example.paymentservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class TuitionServiceClient {
    private RestTemplate restTemplate;
    private String tuitionBaseUrl;
    public TuitionServiceClient(RestTemplate restTemplate,
                                @Qualifier("tuitionBaseUrl") String tuitionBaseUrl) {
        this.restTemplate = restTemplate;
        this.tuitionBaseUrl = tuitionBaseUrl;
    }
    public TuitionInfo getTuition(String mssv){
        String tuitionURL = tuitionBaseUrl + "/students/{mssv}/tuition";
        ResponseEntity<TuitionInfo> response = restTemplate.getForEntity(tuitionURL, TuitionInfo.class, mssv);
        return response.getBody();
    }
    public Boolean updateTuitionStatus(String mssv, String transactionId, BigDecimal amount){
        String tuitionURL = tuitionBaseUrl + "/students/{mssv}/status";
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("transactionId", transactionId);
        body.put("amount", amount); // amount phải âm
        ResponseEntity<java.util.Map> response = restTemplate.exchange(
                tuitionURL,
                HttpMethod.PUT,
                new HttpEntity<>(body),
                java.util.Map.class,
                mssv
        );
        // Chỉ dựa vào mã HTTP: 2xx => true; 4xx giữ nguyên ném lỗi bởi RestTemplate
        return response.getStatusCode().is2xxSuccessful();
    }
    public LockResponse lockTuition(String mssv, java.math.BigInteger userId){
        String url =  tuitionBaseUrl + "/students/{mssv}/lock";
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("mssv", mssv);
        body.put("userId", userId);
        ResponseEntity<LockResponse> response = restTemplate.postForEntity(url, body, LockResponse.class, mssv);
        return response.getBody();
    }
    public Boolean unlockTuition(String mssv, String lockKey){
        String url =  tuitionBaseUrl + "/students/{mssv}/unlock";
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("mssv", mssv);
        body.put("lockKey", lockKey);
        ResponseEntity<UnlockResponse> response = restTemplate.postForEntity(url, body, UnlockResponse.class, mssv);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getUnlocked());
    }

    // DTOs to deserialize TuitionService responses
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
    public static class UpdateStatusResponse {
        private Boolean success;
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
    }

    public static class TuitionInfo {
        private String mssv;
        private String fullName;
        private java.math.BigDecimal tuitionFee;
        private String status;
        public String getMssv() { return mssv; }
        public void setMssv(String mssv) { this.mssv = mssv; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public java.math.BigDecimal getTuitionFee() { return tuitionFee; }
        public void setTuitionFee(java.math.BigDecimal tuitionFee) { this.tuitionFee = tuitionFee; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
