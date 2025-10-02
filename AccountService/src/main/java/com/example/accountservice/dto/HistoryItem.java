package com.example.accountservice.dto;

public class HistoryItem {
	private Long transactionId;
	private String type;
	private String description;
	private Long amount;
	private String timestamp;

	public Long getTransactionId() { return transactionId; }
	public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public Long getAmount() { return amount; }
	public void setAmount(Long amount) { this.amount = amount; }

	public String getTimestamp() { return timestamp; }
	public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}


