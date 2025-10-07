package com.example.accountservice.controller;

import com.example.accountservice.dto.*;
import com.example.accountservice.model.Transaction;
import com.example.accountservice.service.AccountDomainService;
import com.example.accountservice.service.RedisLockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping
public class AccountController {
	private AccountDomainService accountService;
    private final RedisLockService redisLockService;

	public AccountController(AccountDomainService accountService, RedisLockService redisLockService) {
		this.accountService = accountService;
        this.redisLockService = redisLockService;
	}

	@GetMapping("/accounts/{userId}/balance")
	public ResponseEntity<?> getBalance(@PathVariable("userId") BigInteger userId) {
		if (userId == null || userId.compareTo(BigInteger.ZERO) < 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId"));
		}
		var accountOpt = accountService.getAccount(userId);
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User not found"));
		}
		return ResponseEntity.ok(accountOpt.get().getBalance());
	}

    // removed checkBalance to match spec and avoid duplicate validation

	@GetMapping("/accounts/{userId}/history")
	public ResponseEntity<?> getHistory(@PathVariable("userId") BigInteger userId) {
		if (userId == null || userId.compareTo(BigInteger.ZERO) < 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId"));
		}
		ArrayList<Transaction> list = new ArrayList<>(accountService.getHistory(userId));
		ArrayList<HistoryItem> result = new ArrayList<>();
		for (Transaction tx : list) {
			HistoryItem item = new HistoryItem();
            item.setTransactionId(tx.getId());
			item.setType(tx.getType());
			item.setDescription(tx.getDescription());
			item.setAmount(tx.getAmount());
			item.setTimestamp(tx.getTimestamp().toString());
			result.add(item);
		}
		return ResponseEntity.ok(result);
	}

    @PostMapping("/transactions")
    public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest req) {
        if (req.getUserId() == null || req.getMssv() == null || req.getMssv().isEmpty() || req.getType() == null || req.getType().isEmpty() || req.getAmount() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid input data"));
        }
        var accountOpt = accountService.getAccount(req.getUserId());
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User not found"));
        }
        Transaction tx = accountService.saveTransaction(req); // backend sẽ tự set id/status
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("transactionId", tx.getId());
        res.put("timestamp", tx.getTimestamp() != null ? tx.getTimestamp().toString() : null);
        res.put("status", tx.getStatus());
        return ResponseEntity.ok(res);
    }

    // removed manual update status endpoint per simplified spec

	@PutMapping("/accounts/{userId}/balance")
	public ResponseEntity<?> updateBalance(@PathVariable("userId") BigInteger userId,
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
			BigDecimal signedAmount = req.getAmount();
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

    @PostMapping("/accounts/{userId}/lock")
    public ResponseEntity<?> lock(@PathVariable("userId") BigInteger userId) {
        if (userId == null || userId.compareTo(BigInteger.ZERO) < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId"));
        }
		
		// Check if user exists
		var accountOpt = accountService.getAccount(userId);
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User or transaction not found"));
		}

        String lockKey = "lock_" + userId + "_" + System.currentTimeMillis();
        boolean locked = redisLockService.lockAccount(userId, lockKey);
        if (!locked) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error(409, "Account already locked"));
        }

        LockResponse response = new LockResponse();
        response.setLocked(true);
        response.setLockKey(lockKey);
        // TTL trong RedisLockService đang là 120s
        response.setExpiry(System.currentTimeMillis() + 120000);
        return ResponseEntity.ok(response);
	}

    @PostMapping("/accounts/{userId}/unlock")
    public ResponseEntity<?> unlock(@PathVariable("userId") BigInteger userId, @RequestBody UnlockRequest request) {
        if (userId == null || userId.compareTo(BigInteger.ZERO) < 0 || request == null || request.getUserId() == null || 
            request.getLockKey() == null || request.getLockKey().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId or lockKey"));
        }
        if (!userId.equals(request.getUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId or lockKey"));
        }
		
		// Check if user exists
		var accountOpt = accountService.getAccount(userId);
		if (accountOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "User or lock not found"));
		}
		
        boolean unlocked = redisLockService.unlockAccount(userId, request.getLockKey());
        if (!unlocked) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid userId or lockKey"));
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


