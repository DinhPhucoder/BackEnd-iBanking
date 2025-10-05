package com.example.accountservice.dto;

import java.math.BigInteger;
import java.math.BigDecimal;

public class BalanceResponse {
    private BigInteger userId;
    private BigDecimal newBalance;

    public BigInteger getUserId() { return userId; }
    public void setUserId(BigInteger userId) { this.userId = userId; }

    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }
}