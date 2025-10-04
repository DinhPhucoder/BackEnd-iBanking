package com.example.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
	private Long userId;
	private String mssv;
	private String type;
	private Long amount;
	private String description;
	private String transactionId;
}


