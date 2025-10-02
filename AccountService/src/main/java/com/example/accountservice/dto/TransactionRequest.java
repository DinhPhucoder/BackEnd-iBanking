package com.example.accountservice.dto;

public class TransactionRequest {
	private Long userId;
	private String type;
	private Long amount;
	private String description;
	private String transactionId;

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public Long getAmount() { return amount; }
	public void setAmount(Long amount) { this.amount = amount; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getTransactionId() { return transactionId; }
	public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}


