package com.example.accountservice.dto;

import java.math.BigDecimal;

public class BalanceUpdateRequest {
    private BigDecimal amount;
    private String transactionId;
    private BigInteger userId;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigInteger getUserId() { return userId; }
    public void setUserId(BigInteger userId) { this.userId = userId; }
}
