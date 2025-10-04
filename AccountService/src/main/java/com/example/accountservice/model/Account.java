package com.example.accountservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;
import java.math.BigDecimal;


@Data
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor

public class Account {
	@Id
	@Column(name = "accountNumber", unique = true)
	private BigInteger accountNumber;

	@Column(name = "userId", nullable = false, unique = true)
	private BigInteger userId;

	@Column(nullable = false, unique = false)
	private BigDecimal balance;
}


