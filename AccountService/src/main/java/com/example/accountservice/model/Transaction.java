package com.example.accountservice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column
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
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public String getMssv() { return mssv; }
	public void setMssv(String mssv) { this.mssv = mssv; }

	public Long getAmount() { return amount; }
	public void setAmount(Long amount) { this.amount = amount; }

	public Instant getTimestamp() { return timestamp; }
	public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
}


