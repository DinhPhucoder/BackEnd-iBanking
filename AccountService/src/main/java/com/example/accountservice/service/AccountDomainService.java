package com.example.accountservice.service;

import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.model.Account;
import com.example.accountservice.model.Transaction;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class AccountDomainService {
	private AccountRepository accountRepository;
	private TransactionRepository transactionRepository;

	public AccountDomainService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	public Optional<Account> getAccount(BigInteger userId) {
		return accountRepository.findByUserId(userId);
	}

    public ArrayList<Transaction> getHistory(BigInteger userId) {
        return new ArrayList<>(transactionRepository.findByUserIdAndStatusOrderByTimestampDesc(userId, "SUCCESS"));
    }

	public Transaction saveTransaction(TransactionRequest req) {
		Transaction tx = new Transaction();
		tx.setUserId(req.getUserId());
		tx.setAmount(req.getAmount());
		tx.setType(req.getType());
		tx.setDescription(req.getDescription());
		tx.setMssv(req.getMssv());
		return transactionRepository.save(tx);
	}

	@Transactional
	public Account updateBalance(BigInteger userId, BigDecimal amount) {
		Account account = accountRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		BigDecimal newBalance = account.getBalance().add(amount);
		if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Invalid amount or insufficient balance");
		}
		account.setBalance(newBalance);
		return accountRepository.save(account);
	}
}


