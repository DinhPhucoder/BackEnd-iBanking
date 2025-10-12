package com.example.paymentservice.service;

import com.example.paymentservice.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import com.example.paymentservice.client.*;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class PaymentOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(PaymentOrchestratorService.class);
    private AccountServiceClient accountServiceClient;
    private TuitionServiceClient tuitionServiceClient;
    private OtpNotificationServiceClient otpNotificationServiceClient;

    private final ConcurrentHashMap<String, PendingPayment> pendingPayments = new ConcurrentHashMap<>();

    private static class PendingPayment {
        private final PaymentInitRequest init;
        private final String tuitionLockKey;
        private final String accountLockKey;
        private final long createdAtMs;
        private final String otpId;
        private final AtomicBoolean isProcessing = new AtomicBoolean(false);

        PendingPayment(PaymentInitRequest init, String tuitionLockKey, String accountLockKey, String otpId) {
            this.init = init;
            this.tuitionLockKey = tuitionLockKey;
            this.accountLockKey = accountLockKey;
            this.createdAtMs = System.currentTimeMillis();
            this.otpId = otpId;
        }
    }


    PaymentOrchestratorService(AccountServiceClient accountServiceClient, TuitionServiceClient tuitionServiceClient, OtpNotificationServiceClient otpNotificationServiceClient) {
        this.accountServiceClient = accountServiceClient;
        this.tuitionServiceClient = tuitionServiceClient;
        this.otpNotificationServiceClient = otpNotificationServiceClient;
    }

    public String getTransactionId() {
        return UUID.randomUUID().toString();
    }

    public PaymentInitResponse initiate(PaymentInitRequest request){
        if (request == null || request.getUserId() == null || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) == 0
                || request.getMssv() == null || request.getMssv().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId, mssv, or amount");
        }

        if(!accountServiceClient.getAccount(request.getUserId())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or student not found");
        }

        TuitionServiceClient.TuitionInfo tuitionInfo = tuitionServiceClient.getTuition(request.getMssv());
        if (tuitionInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or student not found");
        }
        com.example.paymentservice.client.AccountServiceClient.LockResponse accLock = accountServiceClient.lockUser(request.getUserId());
        if(accLock == null || !Boolean.TRUE.equals(accLock.getLocked())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in another transaction or locked");
        }

        try {
            com.example.paymentservice.client.TuitionServiceClient.LockResponse tuitionLock = tuitionServiceClient.lockTuition(request.getMssv());

            String transactionId = getTransactionId();
            OtpNotificationServiceClient.GenerateResponse otpRes = otpNotificationServiceClient.generateOtp(request.getUserId(), transactionId);
            String otpId = otpRes != null ? otpRes.getOtpId() : null;

            otpNotificationServiceClient.sendEmailOtp(request.getUserId(), otpId);

            PendingPayment store = new PendingPayment(request, tuitionLock.getLockKey(), accLock.getLockKey(), otpId);
            pendingPayments.put(transactionId, store);

            return PaymentInitResponse.builder()
                    .transactionId(transactionId)
                    .otpId(otpId)
                    .build();
        } catch (HttpClientErrorException.Conflict ex) {
            log.warn("[initiate] Conflict when trying to lock tuition for mssv={}. Rolling back user lock.", request.getMssv());
            rollbackUserLock(request.getUserId(), accLock.getLockKey(), "tuition lock conflict");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tuition already paid or locked by another transaction.");

        } catch (Exception ex) {
            log.error("[initiate] Downstream service failed unexpectedly for userId={}, mssv={}. Rolling back user lock.", request.getUserId(), request.getMssv(), ex);
            rollbackUserLock(request.getUserId(), accLock.getLockKey(), "downstream service failure");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A required service is currently unavailable. Please try again later.");
        }
    }

    private void rollbackUserLock(BigInteger userId, String lockKey, String reason) {
        try {
            accountServiceClient.unlockUser(userId, lockKey);
        } catch (Exception unlockEx) {
            log.error("[rollbackUserLock] CRITICAL: Failed to roll back user lock for userId={} after {}. Manual intervention may be required.", userId, reason, unlockEx);
        }
    }

    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        if (request == null || request.getOtpCode() == null || request.getOtpCode().isBlank() ||
            request.getOtpId() == null || request.getTransactionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transactionId, otpId, or otpCode");
        }

        String transactionIdStr = request.getTransactionId();
        PendingPayment pending = pendingPayments.get(transactionIdStr);

        if (pending == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found or already completed.");
        }

        // Atomically acquire the processing lock.
        if (!pending.isProcessing.compareAndSet(false, true)) {
            log.warn("[confirm] tx={} is already being processed by another request.", transactionIdStr);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction is currently being processed. Please wait.");
        }

        try {
            PaymentInitRequest init = pending.init;
            BigInteger userId = init.getUserId();
            String mssv = init.getMssv();
            BigDecimal amount = init.getAmount();

            long now = System.currentTimeMillis();
            if (now - pending.createdAtMs >= 480_000L) {
                log.warn("[confirm] tx={} expired after {} ms", transactionIdStr, (now - pending.createdAtMs));
                pendingPayments.remove(transactionIdStr); // Final state, remove.
                try { accountServiceClient.saveFailedTransaction(userId, mssv, transactionIdStr, amount, "Quá thời gian giao dịch"); } catch (Exception ignore) {}
                try { tuitionServiceClient.unlockTuition(mssv, pending.tuitionLockKey); } catch (Exception ignore) {}
                try { accountServiceClient.unlockUser(userId, pending.accountLockKey); } catch (Exception ignore) {}
                return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
            }

            boolean valid = otpNotificationServiceClient.verifyOtp(request.getOtpId(), request.getOtpCode());
            if (!valid) {
                log.warn("[confirm] tx={} invalid OTP provided. Allowing for retry.", transactionIdStr);
                // DO NOT remove from pendingPayments, just release the lock and let the user retry.
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
            }

            // --- OTP is valid, proceed to final transaction --- //
            pendingPayments.remove(transactionIdStr); // From this point, the transaction is final and cannot be retried.

            try {
                BigDecimal balanceDelta = amount.signum() > 0 ? amount.negate() : amount;
                boolean balanceOk = accountServiceClient.updateBalance(userId, balanceDelta, transactionIdStr);
                if (!balanceOk) {
                    log.warn("[confirm] tx={} updateBalance failed", transactionIdStr);
                    try { accountServiceClient.saveFailedTransaction(userId, mssv, transactionIdStr, amount, "Số dư không đủ"); } catch (Exception ignored) {}
                    tuitionServiceClient.unlockTuition(mssv, pending.tuitionLockKey);
                    accountServiceClient.unlockUser(userId, pending.accountLockKey);
                    return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
                }

                boolean tuitionOk = tuitionServiceClient.updateTuitionStatus(mssv, transactionIdStr, amount);
                if (!tuitionOk) {
                    log.warn("[confirm] tx={} updateTuitionStatus failed -> rollback", transactionIdStr);
                    try { accountServiceClient.saveFailedTransaction(userId, mssv, transactionIdStr, amount, "Cập nhật học phí thất bại"); } catch (Exception ignored) {}
                    accountServiceClient.updateBalance(userId, amount, transactionIdStr); // rollback
                    tuitionServiceClient.unlockTuition(mssv, pending.tuitionLockKey);
                    accountServiceClient.unlockUser(userId, pending.accountLockKey);
                    return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
                }

                // Success
                try { accountServiceClient.saveTransaction(userId, mssv, transactionIdStr, amount); } catch (Exception ignored) {}
                try { otpNotificationServiceClient.sendEmailConfirmation(userId, transactionIdStr, amount.negate(), mssv); } catch (Exception ignored) {}
                log.info("[confirm] tx={} SUCCESS", transactionIdStr);
                tuitionServiceClient.unlockTuition(mssv, pending.tuitionLockKey);
                accountServiceClient.unlockUser(userId, pending.accountLockKey);
                return PaymentConfirmResponse.builder().status("success").transactionId(transactionIdStr).build();

            } catch (Exception ex) {
                log.error("[confirm] tx={} unexpected error: {}. Full rollback.", transactionIdStr, ex.getMessage(), ex);
                try { accountServiceClient.saveFailedTransaction(userId, mssv, transactionIdStr, amount, "Lỗi hệ thống: " + ex.getMessage()); } catch (Exception ignored) {}
                try { accountServiceClient.updateBalance(userId, amount, transactionIdStr); } catch (Exception ignore) {}
                try { tuitionServiceClient.unlockTuition(mssv, pending.tuitionLockKey); } catch (Exception ignore) {}
                try { accountServiceClient.unlockUser(userId, pending.accountLockKey); } catch (Exception ignore) {}
                return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
            }
        } finally {
            // Always release the processing lock if the object still exists.
            if (pending != null) {
                pending.isProcessing.set(false);
            }
        }
    }

    public PaymentCancelResponse cancel(String transactionId) {
        log.info("[cancel] start tx={}", transactionId);
        PendingPayment pending = pendingPayments.remove(transactionId);

        if (pending == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found or already completed");
        }

        PaymentInitRequest init = pending.init;
        log.info("[cancel] Acquired lock for tx={}. Proceeding with cancellation.", transactionId);

        try {
            tuitionServiceClient.unlockTuition(init.getMssv(), pending.tuitionLockKey);
            log.info("[cancel] tx={} unlocked tuition for mssv={}", transactionId, init.getMssv());
        } catch (Exception e) {
            log.error("[cancel] tx={} failed to unlock tuition for mssv={}: {}", transactionId, init.getMssv(), e.getMessage());
        }

        try {
            accountServiceClient.unlockUser(init.getUserId(), pending.accountLockKey);
            log.info("[cancel] tx={} unlocked user {}", transactionId, init.getUserId());
        } catch (Exception e) {
            log.error("[cancel] tx={} failed to unlock user {}: {}", transactionId, init.getUserId(), e.getMessage());
        }

        try {
            accountServiceClient.saveFailedTransaction(init.getUserId(), init.getMssv(), transactionId, init.getAmount(), "Giao dịch bị hủy bởi người dùng");
        } catch (Exception ignore) {
            log.warn("[cancel] tx={} failed to save cancellation record", transactionId);
        }

        return PaymentCancelResponse.builder()
                .transactionId(transactionId)
                .status("cancelled")
                .build();
    }
}
