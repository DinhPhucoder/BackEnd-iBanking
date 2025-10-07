package com.example.paymentservice.service;

import com.example.paymentservice.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.paymentservice.client.*;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PaymentOrchestratorService {
    //    khởi tạo các client tương tác
    private AccountServiceClient accountServiceClient;
    private TuitionServiceClient tuitionServiceClient;
    private OtpNotificationServiceClient otpNotificationServiceClient;

    //    lưu giao dịch tạm thời trong ConcurrentHashMap
    private ConcurrentHashMap<String, PendingPayment> pendingPayments = new ConcurrentHashMap<>();

    private static class PendingPayment {
        private PaymentInitRequest init;
        private String lockKey;
        private String accountLockKey;
        private long createdAtMs;
        private String accountRecordId; // id bản ghi transaction bên AccountService để update status
        PendingPayment(PaymentInitRequest init, String lockKey, String accountLockKey, String accountRecordId) {
            this.init = init;
            this.lockKey = lockKey;
            this.accountLockKey = accountLockKey;
            this.createdAtMs = System.currentTimeMillis();
            this.accountRecordId = accountRecordId;
        }
    }


    PaymentOrchestratorService(AccountServiceClient accountServiceClient, TuitionServiceClient tuitionServiceClient, OtpNotificationServiceClient otpNotificationServiceClient) {
        this.accountServiceClient = accountServiceClient;
        this.tuitionServiceClient = tuitionServiceClient;
        this.otpNotificationServiceClient = otpNotificationServiceClient;
    }

    public PaymentInitResponse initiate(PaymentInitRequest request){
        // kiểm tra đầu vào
        if (request == null || request.getUserId() == null || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0
                || request.getMssv() == null || request.getMssv().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId, mssv, or amount");
        }
        // kiểm tra số dư
        if (!accountServiceClient.checkBalance(request.getUserId(), request.getAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        // kiểm tra user tồn tại
        if(!accountServiceClient.getAccount(request.getUserId())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or student not found");
        }
        
        // lấy thông tin học phí từ TuitionService và validate amount khớp tuitionFee
        com.example.paymentservice.client.TuitionServiceClient.TuitionInfo tuitionInfo = tuitionServiceClient.getTuition(request.getMssv());
        if (tuitionInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or student not found");
        }
        if (tuitionInfo.getTuitionFee() == null || request.getAmount().compareTo(tuitionInfo.getTuitionFee()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId, mssv, or amount");
        }
        // khóa user
        com.example.paymentservice.client.AccountServiceClient.LockResponse accLock = accountServiceClient.lockUser(request.getUserId());
        if(accLock == null || !Boolean.TRUE.equals(accLock.getLocked())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tuition already paid or locked");
        }
        // khóa mssv để tránh trùng thanh toán, nhận lockKey để dùng khi unlock
        com.example.paymentservice.client.TuitionServiceClient.LockResponse lockResponse = tuitionServiceClient.lockTuition(request.getMssv(), request.getUserId());
        if(lockResponse == null || !Boolean.TRUE.equals(lockResponse.getLocked())){
            accountServiceClient.unlockUser(request.getUserId(), accLock.getLockKey());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tuition already paid or locked");
        }
        String transactionId = java.util.UUID.randomUUID().toString(); // Sử dụng UUID làm ID hiển thị/đối ngoại
        com.example.paymentservice.client.OtpNotificationServiceClient.GenerateResponse otpRes = otpNotificationServiceClient.generateOtp(request.getUserId(), transactionId);
        BigInteger otpId = otpRes != null ? otpRes.getOtpId() : BigInteger.valueOf(System.currentTimeMillis());
        // Lưu transaction pending vào AccountService và giữ id để cập nhật
        String pendingRecordId = null;
        try { pendingRecordId = accountServiceClient.savePendingTransaction(request.getUserId(), request.getMssv(), transactionId, request.getAmount()); } catch (Exception ignore) {}
        // Gửi email OTP (nếu service trả kèm mã, ở đây mock: không có otpCode nên bỏ qua)
        // Lưu kèm lockKey để sử dụng khi confirm/rollback
        PendingPayment store = new PendingPayment(request, lockResponse.getLockKey(), accLock.getLockKey(), pendingRecordId);
        pendingPayments.put(transactionId, store);
        return PaymentInitResponse.builder()
                .transactionId(transactionId)
                .otpId(otpId)
                .expiresAt(otpRes != null ? otpRes.getExpiresAt() : null)
                .build();
    }

    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        // Validate input
        if (request == null || request.getOtpCode() == null || request.getOtpCode().isBlank() || 
            request.getOtpId() == null || request.getTransactionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transactionId, otpId, or otpCode");
        }
        
        // Validate OTP code format (6 digits)
        if (!request.getOtpCode().matches("\\d{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transactionId, otpId, or otpCode");
        }
        
        String transactionIdStr = request.getTransactionId();
        PendingPayment pending = pendingPayments.get(transactionIdStr);
        if (pending == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }
        PaymentInitRequest init = pending.init;
        
        // Kiểm tra hết hạn giao dịch (120s) trước khi verify OTP
        long now = System.currentTimeMillis();
        if (now - pending.createdAtMs >= 120_000L) {
            // cập nhật failed cho bản ghi pending thay vì tạo bản ghi mới
            try { accountServiceClient.updateTransactionStatus(pending.accountRecordId, "failed", "Quá thời gian giao dịch"); } catch (Exception ignore) {}
            try { tuitionServiceClient.unlockTuition(init.getMssv(), pending.lockKey); } catch (Exception ignore) {}
            try { accountServiceClient.unlockUser(init.getUserId(), pending.accountLockKey); } catch (Exception ignore) {}
            pendingPayments.remove(transactionIdStr);
            return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
        }

        // verify OTP qua OTPNotificationService
        boolean valid = otpNotificationServiceClient.verifyOtp(request.getOtpId().toString(), request.getOtpCode());
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        BigInteger userId = init.getUserId();
        String mssv = init.getMssv();
        BigDecimal amount = init.getAmount();

        try {
            boolean balanceOk = accountServiceClient.updateBalance(userId, amount.negate(), request.getTransactionId());
            if (!balanceOk) {
                // cập nhật failed - Số dư không đủ
                try { if (pending.accountRecordId != null) accountServiceClient.updateTransactionStatus(pending.accountRecordId, "failed", "Số dư không đủ"); } catch (Exception ignored) {}
                tuitionServiceClient.unlockTuition(mssv, pending.lockKey);
                accountServiceClient.unlockUser(userId, pending.accountLockKey);
                pendingPayments.remove(transactionIdStr);
                return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
            }
            // gửi amount âm để gạch nợ theo mô tả
            boolean tuitionOk = tuitionServiceClient.updateTuitionStatus(mssv, transactionIdStr, amount.negate());
            if (!tuitionOk) {
                // cập nhật failed - Cập nhật học phí lỗi
                try { if (pending.accountRecordId != null) accountServiceClient.updateTransactionStatus(pending.accountRecordId, "failed", "Cập nhật học phí thất bại"); } catch (Exception ignored) {}
                accountServiceClient.updateBalance(userId, amount, request.getTransactionId()); // rollback
                tuitionServiceClient.unlockTuition(mssv, pending.lockKey);
                accountServiceClient.unlockUser(userId, pending.accountLockKey);
                pendingPayments.remove(transactionIdStr);
                return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
            }

            // cập nhật transaction thành công
            try { if (pending.accountRecordId != null) accountServiceClient.updateTransactionStatus(pending.accountRecordId, "success", "Thanh toán học phí " + transactionIdStr); } catch (Exception ignored) {}
            pendingPayments.remove(transactionIdStr);
            tuitionServiceClient.unlockTuition(mssv, pending.lockKey);
            accountServiceClient.unlockUser(userId, pending.accountLockKey);

            return PaymentConfirmResponse.builder().status("success").transactionId(transactionIdStr).build();
        } catch (Exception ex) {
            // cập nhật failed - Exception hệ thống
            try { if (pending.accountRecordId != null) accountServiceClient.updateTransactionStatus(pending.accountRecordId, "failed", "Lỗi hệ thống: " + ex.getMessage()); } catch (Exception ignored) {}
            try { accountServiceClient.updateBalance(userId, amount, request.getTransactionId()); } catch (Exception ignore) {}
            try { tuitionServiceClient.unlockTuition(mssv, pending.lockKey); } catch (Exception ignore) {}
            try { accountServiceClient.unlockUser(userId, pending.accountLockKey); } catch (Exception ignore) {}
            pendingPayments.remove(transactionIdStr);
            return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
        }
    }
}