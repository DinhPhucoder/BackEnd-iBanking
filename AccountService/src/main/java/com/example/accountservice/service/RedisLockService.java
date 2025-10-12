package com.example.accountservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "account:lock:";
    private static final long LOCK_EXPIRE_TIME = 240; // 240 giây
    
    public boolean lockAccount(BigInteger userId, String lockKey) {
        String lockName = LOCK_PREFIX + userId;
        
        // Sử dụng setIfAbsent để đảm bảo thao tác là nguyên tử (atomic)
        // Nó sẽ chỉ set key nếu key đó chưa tồn tại.
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockName, lockKey, LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
        
        // setIfAbsent có thể trả về null trong một số trường hợp, nên kiểm tra bằng Boolean.TRUE
        return Boolean.TRUE.equals(success);
    }
    
    public boolean unlockAccount(BigInteger userId, String lockKey) {
        String lockName = LOCK_PREFIX + userId;
        
        // Kiểm tra lock key có đúng không
        Object currentLockKey = redisTemplate.opsForValue().get(lockName);
        if (currentLockKey == null || !currentLockKey.toString().equals(lockKey)) {
            return false; // Lock key không đúng hoặc đã hết hạn
        }
        
        // Xóa lock
        redisTemplate.delete(lockName);
        return true;
    }
    
    public boolean isAccountLocked(BigInteger userId) {
        String lockName = LOCK_PREFIX + userId;
        return redisTemplate.hasKey(lockName);
    }
    
    public String getLockKey(BigInteger userId) {
        String lockName = LOCK_PREFIX + userId;
        Object lockKey = redisTemplate.opsForValue().get(lockName);
        return lockKey != null ? lockKey.toString() : null;
    }
}
