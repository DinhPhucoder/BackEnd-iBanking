package com.example.accountservice.dto;

public class BalanceResponse {
	private Long balance;

	public BalanceResponse() {}

	public BalanceResponse(Long balance) {
		this.balance = balance;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}
}


