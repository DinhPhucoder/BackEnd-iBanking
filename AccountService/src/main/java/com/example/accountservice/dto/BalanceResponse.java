package com.example.accountservice.dto;

public class BalanceResponse {
    private Long userId;
    private Long newBalance;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getNewBalance() { return newBalance; }
    public void setNewBalance(Long newBalance) { this.newBalance = newBalance; }
}