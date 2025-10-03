package com.example.paymentservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TuitionServiceClient {
    private RestTemplate restTemplate;
    private String tuitionBaseUrl;
    public TuitionServiceClient(RestTemplate restTemplate,
                                @Qualifier("tuitionBaseUrl") String tuitionBaseUrl) {
        this.restTemplate = restTemplate;
        this.tuitionBaseUrl = tuitionBaseUrl;
    }
    public boolean checkStudentExists(String mssv){
        String tuitionURL = tuitionBaseUrl + "/students/{mssv}/exists";
        ResponseEntity<Boolean> response = restTemplate.getForEntity(tuitionURL, Boolean.class, mssv);
        return response.getBody() != null && response.getBody();
    }
    public Boolean updateTuitionStatus(String mssv, String transactionId, Long amount){
        String tuitionURL = tuitionBaseUrl + "/students/{mssv}/status";
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("transactionId", transactionId);
        body.put("mssv", mssv);
        body.put("amount", amount);
        ResponseEntity<UpdateStatusResponse> response = restTemplate.exchange(
                tuitionURL,
                HttpMethod.PUT,
                new HttpEntity<>(body),
                UpdateStatusResponse.class,
                mssv
        );
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess());
    }
    public Boolean lockTuition(String mssv){
        String url =  tuitionBaseUrl + "/students/{mssv}/lock";
        ResponseEntity<LockResponse> response = restTemplate.postForEntity(url, null, LockResponse.class, mssv);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getLocked());
    }
    public Boolean unlockTuition(String mssv){
        String url =  tuitionBaseUrl + "/students/{mssv}/unlock";
        ResponseEntity<UnlockResponse> response = restTemplate.postForEntity(url, null, UnlockResponse.class, mssv);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getUnlocked());
    }

    // DTOs to deserialize TuitionService responses
    public static class LockResponse {
        private Boolean locked;
        private String lockKey;
        public Boolean getLocked() { return locked; }
        public void setLocked(Boolean locked) { this.locked = locked; }
        public String getLockKey() { return lockKey; }
        public void setLockKey(String lockKey) { this.lockKey = lockKey; }
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
}
