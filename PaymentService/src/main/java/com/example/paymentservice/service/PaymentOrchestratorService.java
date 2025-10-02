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
    private final AccountServiceClient accountServiceClient;
    private final TuitionServiceClient tuitionServiceClient;
    private final OtpNotificationServiceClient otpNotificationServiceClient;

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");
        }
        // kiểm tra số dư
        if (!accountServiceClient.checkBalance(request.getUserId(), request.getAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số dư không đủ");
        }

        // kiểm tra sinh viên
        if(!tuitionServiceClient.checkStudentExists(request.getMssv())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MSSV không tồn tại");
        }
        // khóa user
        if(!accountServiceClient.lockUser(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tài khoản đang bị khóa");
        }
        // check học phí
        if(!tuitionServiceClient.lockTuition(request.getMssv())){
            accountServiceClient.unlockUser(request.getUserId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "MSSV đang bị khóa");
        }
        String transactionId = UUID.randomUUID().toString();
        String otpId = otpNotificationServiceClient.generateOtp(request.getUserId(), transactionId);
        pendingPayments.put(transactionId, request);
        return PaymentInitResponse.builder()
                .transactionId(transactionId)
                .otpId(otpId)
                .build();
    }

    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        if (request == null || request.getOtpCode() == null || request.getOtpCode().isBlank() || request.getOtpId() == null || request.getOtpId().isBlank() || request.getTransactionId() == null || request.getTransactionId().isBlank()) {
            return PaymentConfirmResponse.builder().status("FAILED").transactionId(null).build();
        }
        boolean valid = otpNotificationServiceClient.verifyOtp(request.getOtpId(), request.getOtpCode());
        if (!valid) {
            return PaymentConfirmResponse.builder().status("FAILED").transactionId(null).build();
        }
        String transactionId = request.getTransactionId();
        PaymentInitRequest init = pendingPayments.get(transactionId);
        if (init == null) {
            return PaymentConfirmResponse.builder().status("FAILED").transactionId(null).build();
        }

        Long userId = init.getUserId();
        String mssv = init.getMssv();
        Long amount = init.getAmount();

        try {
            boolean balanceOk = accountServiceClient.updateBalance(userId, -amount, transactionId);
            if (!balanceOk) {
                tuitionServiceClient.unlockTuition(mssv);
                accountServiceClient.unlockUser(userId);
                return PaymentConfirmResponse.builder().status("FAILED").transactionId(null).build();
            }

            boolean tuitionOk = tuitionServiceClient.updateTuitionStatus(mssv, transactionId, amount);
            if (!tuitionOk) {
                accountServiceClient.updateBalance(userId, amount, transactionId); // rollback
                tuitionServiceClient.unlockTuition(mssv);
                accountServiceClient.unlockUser(userId);
                return PaymentConfirmResponse.builder().status("FAILED").transactionId(null).build();
            }

            String savedTransactionId = accountServiceClient.saveTransaction(userId, transactionId, amount);
            pendingPayments.remove(transactionId);
            tuitionServiceClient.unlockTuition(mssv);
            accountServiceClient.unlockUser(userId);

            return PaymentConfirmResponse.builder().status("SUCCESS").transactionId(savedTransactionId).build();
        } catch (Exception ex) {
            try { accountServiceClient.updateBalance(userId, amount, transactionId); } catch (Exception ignore) {}
            try { tuitionServiceClient.unlockTuition(mssv); } catch (Exception ignore) {}
            try { accountServiceClient.unlockUser(userId); } catch (Exception ignore) {}
            return PaymentConfirmResponse.builder().status("FAILED").transactionId(null).build();
        }
    }
}