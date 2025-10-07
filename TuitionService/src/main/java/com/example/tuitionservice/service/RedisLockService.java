package com.example.tuitionservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "tuition:lock:";
    private static final long LOCK_EXPIRE_TIME = 120; // 120 giây
    
    public boolean lockTuition(String mssv, String lockKey) {
        String lockName = LOCK_PREFIX + mssv;
        
        // Kiểm tra xem account đã bị lock chưa
        if (redisTemplate.hasKey(lockName)) {
            return false; // Account đã bị lock
        }
        
        // Tạo lock với expire time
        redisTemplate.opsForValue().set(lockName, lockKey, LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
        return true;
    }
    
    public boolean unlockTuition(String mssv, String lockKey) {
        String lockName = LOCK_PREFIX + mssv;
        
        // Kiểm tra lock key có đúng không
        Object currentLockKey = redisTemplate.opsForValue().get(lockName);
        if (currentLockKey == null || !currentLockKey.toString().equals(lockKey)) {
            return false; // Lock key không đúng hoặc đã hết hạn
        }
        
        // Xóa lock
        redisTemplate.delete(lockName);
        return true;
    }
    
    public boolean isTuitionLocked(String mssv) {
        String lockName = LOCK_PREFIX + mssv;
        return redisTemplate.hasKey(lockName);
    }
    
    public String getLockKey(String mssv) {
        String lockName = LOCK_PREFIX + mssv;
        Object lockKey = redisTemplate.opsForValue().get(lockName);
        return lockKey != null ? lockKey.toString() : null;
    }
}
