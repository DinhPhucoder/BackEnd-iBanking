package com.example.accountservice.controller;

import com.example.accountservice.dto.HistoryItem;
import com.example.accountservice.dto.TransactionRequest;
import com.example.accountservice.dto.TransactionResponse;
import com.example.accountservice.model.Transaction;
import com.example.accountservice.service.AccountDomainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class AccountController {
	private final AccountDomainService accountService;

	public AccountController(AccountDomainService accountService) {
		this.accountService = accountService;
	}

	@GetMapping("/accounts/{userId}/balance")
	public ResponseEntity<?> getBalance(@PathVariable("userId") Long userId) {
		if (userId == null || userId < 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId"));
		}
		return accountService.getAccount(userId)
			.map(a -> ResponseEntity.ok(a.getBalance()))
			.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User not found")));
	}

	@PostMapping("/accounts/checkBalance")
	public ResponseEntity<Boolean> checkBalance(@RequestBody TransactionRequest req) {
		if (req.getUserId() == null || req.getAmount() == null) {
			return ResponseEntity.ok(false);
		}
		return accountService.getAccount(req.getUserId())
			.map(a -> ResponseEntity.ok(a.getBalance() >= req.getAmount()))
			.orElse(ResponseEntity.ok(false));
	}

	@GetMapping("/accounts/{userId}/history")
	public ResponseEntity<?> getHistory(@PathVariable("userId") Long userId) {
		if (userId == null || userId < 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId"));
		}
		List<Transaction> list = accountService.getHistory(userId);
		List<HistoryItem> result = list.stream().map(tx -> {
			HistoryItem item = new HistoryItem();
			item.setTransactionId(tx.getId());
			item.setType(tx.getType());
			item.setDescription(tx.getDescription());
			item.setAmount(tx.getAmount());
			item.setTimestamp(tx.getTimestamp().toString());
			return item;
		}).collect(Collectors.toList());
		return ResponseEntity.ok(result);
	}

	@PostMapping("/transactions")
	public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest req) {
		if (req.getUserId() == null || req.getType() == null || req.getType().isEmpty() || req.getAmount() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid input data"));
		}
		return accountService.getAccount(req.getUserId())
			.map(a -> {
				Transaction tx = accountService.saveTransaction(req);
				return ResponseEntity.ok(String.valueOf(tx.getId()));
			})
			.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User or recipient not found")));
	}

	@PutMapping("/accounts/{userId}/balance")
	public ResponseEntity<?> updateBalance(@PathVariable("userId") Long userId,
	                                      @RequestBody TransactionRequest req) {
		if (userId == null || req.getAmount() == null || req.getUserId() == null) {
			return ResponseEntity.ok(false);
		}
		if (!userId.equals(req.getUserId())) {
			return ResponseEntity.ok(false);
		}
		try {
			long signedAmount = req.getAmount();
			accountService.updateBalance(userId, signedAmount);
			return ResponseEntity.ok(true);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.ok(false);
		}
	}

	// Simple in-memory locks for skeleton (can be replaced by Redis later)
	private final java.util.Set<Long> accountLocks = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

	@PostMapping("/accounts/{userId}/lock")
	public ResponseEntity<?> lock(@PathVariable("userId") Long userId) {
		if (userId == null || userId < 0) {
			return ResponseEntity.ok(false);
		}
		synchronized (accountLocks) {
			if (accountLocks.contains(userId)) {
				return ResponseEntity.ok(false);
			}
			accountLocks.add(userId);
		}
		return ResponseEntity.ok(true);
	}

	@PostMapping("/accounts/{userId}/unlock")
	public ResponseEntity<?> unlock(@PathVariable("userId") Long userId) {
		if (userId == null || userId < 0) {
			return ResponseEntity.ok(false);
		}
		accountLocks.remove(userId);
		return ResponseEntity.ok(true);
	}

	private java.util.Map<String, Object> error(int code, String message) {
		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("error", message);
		map.put("code", code);
		return map;
	}
}


