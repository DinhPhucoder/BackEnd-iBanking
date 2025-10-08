package com.example.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
	private BigInteger userId;
	private String mssv;
	private String type;
    private String status; // NEW: SUCCESS | FAILED | PENDING
	private BigDecimal amount;
	private String description;
}


