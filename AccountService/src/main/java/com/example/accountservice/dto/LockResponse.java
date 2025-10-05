package com.example.accountservice.dto;

public class LockResponse {
    private boolean locked;
    private String lockKey;
    private long expiry;

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getLockKey() { return lockKey; }
    public void setLockKey(String lockKey) { this.lockKey = lockKey; }

    public long getExpiry() { return expiry; }
    public void setExpiry(long expiry) { this.expiry = expiry; }
}
