package com.example.accountservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transactionID")
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String mssv;

	@Column(nullable = false)
	private Long amount;

	@Column(nullable = false)
	private Instant timestamp;

	@Column(nullable = false)
	private String status; // pending, success, failed

	@Column(nullable = false)
	private String type; // e.g., TUITION_PAYMENT

	@Column(length = 100)
	private String description;

	public Transaction() {}

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


