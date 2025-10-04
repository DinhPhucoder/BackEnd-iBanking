package com.example.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryItem {
	private BigInteger transactionId;
	private String type;
	private String description;
	private BigDecimal amount;
	private String timestamp;
}


