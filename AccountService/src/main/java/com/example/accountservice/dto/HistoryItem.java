package com.example.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryItem {
	private Long transactionId;
	private String type;
	private String description;
	private Long amount;
	private String timestamp;
}


