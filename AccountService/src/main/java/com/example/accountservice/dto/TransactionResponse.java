package com.example.accountservice.dto;

public class TransactionResponse {
	private Long transactionId;
	private String timestamp;

	public TransactionResponse() {}

	public TransactionResponse(Long transactionId, String timestamp) {
		this.transactionId = transactionId;
		this.timestamp = timestamp;
	}

	public Long getTransactionId() { return transactionId; }
	public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

	public String getTimestamp() { return timestamp; }
	public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}


