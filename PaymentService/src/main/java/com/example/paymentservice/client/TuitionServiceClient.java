package com.example.paymentservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TuitionServiceClient {
    private final RestTemplate restTemplate;
    private final String tuitionBaseUrl;
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
        ResponseEntity<Boolean> response = restTemplate.exchange(
                tuitionURL,
                HttpMethod.PUT,
                new HttpEntity<>(body),
                Boolean.class,
                mssv
        );
        return response.getBody() != null && response.getBody();
    }
    public Boolean lockTuition(String mssv){
        String url =  tuitionBaseUrl + "/students/{mssv}/lock";
        ResponseEntity<Boolean> response = restTemplate.postForEntity(url, null,Boolean.class, mssv);
        return response.getBody() != null && response.getBody();
    }
    public Boolean unlockTuition(String mssv){
        String url =  tuitionBaseUrl + "/students/{mssv}/unlock";
        ResponseEntity<Boolean> response = restTemplate.postForEntity(url, null,Boolean.class, mssv);
        return response.getBody() != null && response.getBody();
    }
}
