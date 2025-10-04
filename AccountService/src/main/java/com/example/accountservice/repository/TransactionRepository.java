package com.example.accountservice.repository;

import com.example.accountservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	List<Transaction> findByUserIdOrderByTimestampDesc(BigInteger userId);
}


