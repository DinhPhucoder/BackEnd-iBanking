package com.example.accountservice.controller;

import com.example.accountservice.dto.*;
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
	private AccountDomainService accountService;

	public AccountController(AccountDomainService accountService) {
		this.accountService = accountService;
	}

	@GetMapping("/accounts/{userId}/balance")
	public ResponseEntity<?> getBalance(@PathVariable("userId") Long userId) {
		if (userId == null || userId < 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId"));
		}
		var accountOpt = accountService.getAccount(userId);
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User not found"));
		}
		return ResponseEntity.ok(accountOpt.get().getBalance());
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
		var accountOpt = accountService.getAccount(req.getUserId());
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User or recipient not found"));
		}
		Transaction tx = accountService.saveTransaction(req);
		return ResponseEntity.ok(String.valueOf(tx.getId()));
	}

	@PutMapping("/accounts/{userId}/balance")
	public ResponseEntity<?> updateBalance(@PathVariable("userId") Long userId,
	                                      @RequestBody BalanceUpdateRequest req) {
		if (userId == null || req.getAmount() == null || req.getUserId() == null || req.getTransactionId() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid amount or insufficient balance"));
		}
		if (!userId.equals(req.getUserId())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid amount or insufficient balance"));
		}
		
		// Check if user exists
		var accountOpt = accountService.getAccount(userId);
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User not found"));
		}
		
		try {
			long signedAmount = req.getAmount();
			accountService.updateBalance(userId, signedAmount);
			
			// Get updated balance
			var updatedAccount = accountService.getAccount(userId);
			BalanceResponse response = new BalanceResponse();
			response.setUserId(userId);
			response.setNewBalance(updatedAccount.get().getBalance());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid amount or insufficient balance"));
		}
	}

	// Simple in-memory locks for skeleton (can be replaced by Redis later)
	private final java.util.Set<Long> accountLocks = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

	@PostMapping("/accounts/{userId}/lock")
	public ResponseEntity<?> lock(@PathVariable("userId") Long userId, @RequestBody LockRequest request) {
		if (userId == null || userId < 0 || request == null || request.getUserId() == null || request.getTransactionId() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId or transactionId"));
		}
		if (!userId.equals(request.getUserId())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId or transactionId"));
		}
		
		// Check if user exists
		var accountOpt = accountService.getAccount(userId);
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User or transaction not found"));
		}
		
		synchronized (accountLocks) {
			if (accountLocks.contains(userId)) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(error(409, "Account already locked"));
			}
			accountLocks.add(userId);
		}
		
		LockResponse response = new LockResponse();
		response.setLocked(true);
		response.setLockKey("lock_" + userId + "_" + request.getTransactionId());
		response.setExpiry(System.currentTimeMillis() + 300000); // 5 minutes
		return ResponseEntity.ok(response);
	}

	@PostMapping("/accounts/{userId}/unlock")
	public ResponseEntity<?> unlock(@PathVariable("userId") Long userId, @RequestBody UnlockRequest request) {
		if (userId == null || userId < 0 || request == null || request.getUserId() == null || 
		    request.getTransactionId() == null || request.getLockKey() == null || request.getLockKey().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId, lockKey, or transactionId"));
		}
		if (!userId.equals(request.getUserId())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId, lockKey, or transactionId"));
		}
		
		// Check if user exists
		var accountOpt = accountService.getAccount(userId);
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User or lock not found"));
		}
		
		boolean removed = accountLocks.remove(userId);
		if (!removed) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User or lock not found"));
		}
		
		UnlockResponse response = new UnlockResponse();
		response.setUnlocked(true);
		return ResponseEntity.ok(response);
	}

	private java.util.Map<String, Object> error(int code, String message) {
		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("error", message);
		map.put("code", code);
		return map;
	}
}


