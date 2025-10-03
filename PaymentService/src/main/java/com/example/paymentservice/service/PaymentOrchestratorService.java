package com.example.paymentservice.service;

import com.example.paymentservice.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.paymentservice.client.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class PaymentOrchestratorService {
    //    khởi tạo các client tương tác
    private AccountServiceClient accountServiceClient;
    private TuitionServiceClient tuitionServiceClient;
    private OtpNotificationServiceClient otpNotificationServiceClient;

    //    lưu giao dịch tạm thời trong ConcurrentHashMap
    private ConcurrentHashMap<String,PaymentInitRequest>  pendingPayments = new ConcurrentHashMap<>();


    PaymentOrchestratorService(AccountServiceClient accountServiceClient, TuitionServiceClient tuitionServiceClient, OtpNotificationServiceClient otpNotificationServiceClient) {
        this.accountServiceClient = accountServiceClient;
        this.tuitionServiceClient = tuitionServiceClient;
        this.otpNotificationServiceClient = otpNotificationServiceClient;
    }

    public PaymentInitResponse initiate(PaymentInitRequest request){
        // kiểm tra đầu vào
        if (request == null || request.getUserId() == null || request.getAmount() == null || request.getAmount() <= 0
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
        
        // kiểm tra sinh viên tồn tại ở TuitionService
        if(!tuitionServiceClient.checkStudentExists(request.getMssv())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or student not found");
        }
        // khóa user
        if(!accountServiceClient.lockUser(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tuition already paid or locked");
        }
        // khóa mssv để tránh trùng thanh toán
        if(!tuitionServiceClient.lockTuition(request.getMssv())){
            accountServiceClient.unlockUser(request.getUserId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tuition already paid or locked");
        }
        Long transactionId = System.currentTimeMillis(); // Sử dụng timestamp làm ID
        Long otpId = otpNotificationServiceClient.generateOtp(request.getUserId(), transactionId.toString());
        pendingPayments.put(transactionId.toString(), request);
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
        
        // Validate OTP code format (6 digits)
        if (!request.getOtpCode().matches("\\d{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transactionId, otpId, or otpCode");
        }
        
        String transactionIdStr = request.getTransactionId().toString();
        PaymentInitRequest init = pendingPayments.get(transactionIdStr);
        if (init == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }
        
        // verify OTP qua OTPNotificationService
        boolean valid = otpNotificationServiceClient.verifyOtp(request.getOtpId().toString(), request.getOtpCode());
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }

        Long userId = init.getUserId();
        String mssv = init.getMssv();
        Long amount = init.getAmount();

        try {
            boolean balanceOk = accountServiceClient.updateBalance(userId, -amount, transactionIdStr);
            if (!balanceOk) {
                tuitionServiceClient.unlockTuition(mssv);
                accountServiceClient.unlockUser(userId);
                return PaymentConfirmResponse.builder().status("failed").transactionId(null).build();
            }
            boolean tuitionOk = tuitionServiceClient.updateTuitionStatus(mssv, transactionIdStr, amount);
            if (!tuitionOk) {
                accountServiceClient.updateBalance(userId, amount, transactionIdStr); // rollback
                tuitionServiceClient.unlockTuition(mssv);
                accountServiceClient.unlockUser(userId);
                return PaymentConfirmResponse.builder().status("failed").transactionId(null).build();
            }

            String savedTransactionId = accountServiceClient.saveTransaction(userId, transactionIdStr, amount);
            pendingPayments.remove(transactionIdStr);
            tuitionServiceClient.unlockTuition(mssv);
            accountServiceClient.unlockUser(userId);

            return PaymentConfirmResponse.builder().status("success").transactionId(savedTransactionId).build();
        } catch (Exception ex) {
            try { accountServiceClient.updateBalance(userId, amount, transactionIdStr); } catch (Exception ignore) {}
            try { tuitionServiceClient.unlockTuition(mssv); } catch (Exception ignore) {}
            try { accountServiceClient.unlockUser(userId); } catch (Exception ignore) {}
            return PaymentConfirmResponse.builder().status("failed").transactionId(null).build();
        }
    }
}