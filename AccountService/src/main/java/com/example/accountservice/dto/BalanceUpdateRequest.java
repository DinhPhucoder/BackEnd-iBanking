package com.example.accountservice.dto;

import java.math.BigInteger;
import java.math.BigDecimal;

public class BalanceUpdateRequest {
    private BigDecimal amount;
    private Long transactionId;
    private BigInteger userId;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public BigInteger getUserId() { return userId; }
    public void setUserId(BigInteger userId) { this.userId = userId; }
}
