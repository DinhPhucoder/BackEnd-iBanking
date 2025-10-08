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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class PaymentOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(PaymentOrchestratorService.class);
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
        PendingPayment(PaymentInitRequest init, String lockKey, String accountLockKey) {
            this.init = init;
            this.lockKey = lockKey;
            this.accountLockKey = accountLockKey;
            this.createdAtMs = System.currentTimeMillis();
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
        // kiểm tra đầu vào
        // Cho phép amount âm hoặc dương, chỉ không cho bằng 0
        if (request == null || request.getUserId() == null || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) == 0
                || request.getMssv() == null || request.getMssv().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId, mssv, or amount");
        }

        // kiểm tra user tồn tại
        if(!accountServiceClient.getAccount(request.getUserId())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or student not found");
        }
        
        // lấy thông tin học phí từ TuitionService và validate amount khớp tuitionFee
        TuitionServiceClient.TuitionInfo tuitionInfo = tuitionServiceClient.getTuition(request.getMssv());
        if (tuitionInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or student not found");
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
        String transactionId = getTransactionId(); // Sử dụng UUID làm ID hiển thị/đối ngoại
        OtpNotificationServiceClient.GenerateResponse otpRes = otpNotificationServiceClient.generateOtp(request.getUserId(), transactionId);
        String otpId = otpRes != null ? otpRes.getOtpId(): null;
        // gửi email OTP ngay sau khi generate thành công
        otpNotificationServiceClient.sendEmailOtp(request.getUserId(), otpId);
        // Lưu kèm lockKey để sử dụng khi confirm/rollback
        PendingPayment store = new PendingPayment(request, lockResponse.getLockKey(), accLock.getLockKey());
        pendingPayments.put(transactionId, store);
        return PaymentInitResponse.builder()
                .transactionId(transactionId)
                .otpId(otpId)
                .build();
    }

    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        // Validate input
        if (request == null || request.getOtpCode() == null || request.getOtpCode().isBlank() || 
            request.getOtpId() == null || request.getTransactionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transactionId, otpId, or otpCode");
        }
        
        String transactionIdStr = request.getTransactionId();
        String maskedOtp = request.getOtpCode() != null && request.getOtpCode().length() >= 2
                ? request.getOtpCode().substring(0, 2) + "****" : "****";
        log.info("[confirm] start tx={}, otpId={}, otpCode={} (masked)", transactionIdStr, request.getOtpId(), maskedOtp);
        PendingPayment pending = pendingPayments.get(transactionIdStr);
        if (pending == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }
        PaymentInitRequest init = pending.init;
        
        // Kiểm tra hết hạn giao dịch (480s) trước khi verify OTP
        long now = System.currentTimeMillis();
        if (now - pending.createdAtMs >= 480_000L) {
            log.warn("[confirm] tx={} expired after {} ms", transactionIdStr, (now - pending.createdAtMs));
            // Hết hạn: tạo transaction thất bại để ghi nhận
            try { accountServiceClient.saveFailedTransaction(init.getUserId(), init.getMssv(), transactionIdStr, init.getAmount(), "Quá thời gian giao dịch"); } catch (Exception ignore) {}
            try { tuitionServiceClient.unlockTuition(init.getMssv(), pending.lockKey); } catch (Exception ignore) {}
            try { accountServiceClient.unlockUser(init.getUserId(), pending.accountLockKey); } catch (Exception ignore) {}
            pendingPayments.remove(transactionIdStr);
            return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
        }

        // verify OTP qua OTPNotificationService
        boolean valid = otpNotificationServiceClient.verifyOtp(request.getOtpId(), request.getOtpCode());
        log.info("[confirm] tx={} otp.verify result={} (otpId={})", transactionIdStr, valid, request.getOtpId());
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        BigInteger userId = init.getUserId();
        String mssv = init.getMssv();
        BigDecimal amount = init.getAmount();

        try {
            // Nếu amount dương thì trừ (negate), nếu amount âm thì dùng nguyên giá trị âm
            BigDecimal balanceDelta = amount.signum() > 0 ? amount.negate() : amount;
            boolean balanceOk = accountServiceClient.updateBalance(userId, balanceDelta, request.getTransactionId());
            log.info("[confirm] tx={} updateBalance result={} delta={}", transactionIdStr, balanceOk, balanceDelta);
            if (!balanceOk) {
                log.warn("[confirm] tx={} updateBalance failed", transactionIdStr);
                // Số dư không đủ: tạo transaction thất bại
                try { accountServiceClient.saveFailedTransaction(userId, mssv, transactionIdStr, amount, "Số dư không đủ"); } catch (Exception ignored) {}
                tuitionServiceClient.unlockTuition(mssv, pending.lockKey);
                accountServiceClient.unlockUser(userId, pending.accountLockKey);
                pendingPayments.remove(transactionIdStr);
                return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
            }
            // Gửi amount đúng như lúc initiate (có thể là âm theo yêu cầu mới)
            boolean tuitionOk = tuitionServiceClient.updateTuitionStatus(mssv, transactionIdStr, amount);
            log.info("[confirm] tx={} updateTuitionStatus result={} amountSent={} mssv={}", transactionIdStr, tuitionOk, amount, mssv);
            if (!tuitionOk) {
                log.warn("[confirm] tx={} updateTuitionStatus failed -> rollback", transactionIdStr);
                // Cập nhật học phí lỗi: tạo transaction thất bại và rollback số dư
                try { accountServiceClient.saveFailedTransaction(userId, mssv, transactionIdStr, amount, "Cập nhật học phí thất bại"); } catch (Exception ignored) {}
                accountServiceClient.updateBalance(userId, amount, request.getTransactionId()); // rollback
                tuitionServiceClient.unlockTuition(mssv, pending.lockKey);
                accountServiceClient.unlockUser(userId, pending.accountLockKey);
                pendingPayments.remove(transactionIdStr);
                return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
            }

            // Thành công: tạo bản ghi transaction thành công tại AccountService
            try { accountServiceClient.saveTransaction(userId, mssv, transactionIdStr, amount); } catch (Exception ignored) {}
            log.info("[confirm] tx={} SUCCESS", transactionIdStr);
            pendingPayments.remove(transactionIdStr);
            tuitionServiceClient.unlockTuition(mssv, pending.lockKey);
            accountServiceClient.unlockUser(userId, pending.accountLockKey);

            return PaymentConfirmResponse.builder().status("success").transactionId(transactionIdStr).build();
        } catch (Exception ex) {
            log.error("[confirm] tx={} error: {}", transactionIdStr, ex.getMessage(), ex);
            // Exception hệ thống: tạo transaction thất bại và rollback nếu cần
            try { accountServiceClient.saveFailedTransaction(userId, mssv, transactionIdStr, amount, "Lỗi hệ thống: " + ex.getMessage()); } catch (Exception ignored) {}
            try { accountServiceClient.updateBalance(userId, amount, request.getTransactionId()); } catch (Exception ignore) {}
            try { tuitionServiceClient.unlockTuition(mssv, pending.lockKey); } catch (Exception ignore) {}
            try { accountServiceClient.unlockUser(userId, pending.accountLockKey); } catch (Exception ignore) {}
            pendingPayments.remove(transactionIdStr);
            return PaymentConfirmResponse.builder().status("failed").transactionId(transactionIdStr).build();
        }
    }
}