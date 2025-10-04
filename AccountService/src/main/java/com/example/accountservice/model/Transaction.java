package com.example.accountservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transactionID")
	private Long id;

	@Column(nullable = false)
	private BigInteger userId;

	@Column(nullable = false)
	private String mssv;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private Instant timestamp;

	@Column(nullable = false)
	private String status; // pending, success, failed

	@Column(nullable = false)
	private String type; // e.g., TUITION_PAYMENT

	@Column(length = 100)
	private String description;


	@PrePersist
	public void onCreate() {
		if (timestamp == null) {
			timestamp = Instant.now();
		}
		if (status == null) {
			status = "pending";
		}
		if (mssv == null) {
			mssv = "DEFAULT_MSSV";
		}
	}

}


