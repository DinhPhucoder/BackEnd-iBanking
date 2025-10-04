package com.example.paymentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@Service
public class RedisPaymentService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String PAYMENT_PREFIX = "payment:";
    private static final long PAYMENT_EXPIRE_TIME = 300; // 5 phút
    
    public void storePendingPayment(BigInteger transactionId, Object paymentData) {
        String key = PAYMENT_PREFIX + transactionId;
        redisTemplate.opsForValue().set(key, paymentData, PAYMENT_EXPIRE_TIME, TimeUnit.SECONDS);
    }
    
    public Object getPendingPayment(BigInteger transactionId) {
        String key = PAYMENT_PREFIX + transactionId;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void removePendingPayment(BigInteger transactionId) {
        String key = PAYMENT_PREFIX + transactionId;
        redisTemplate.delete(key);
    }
}
