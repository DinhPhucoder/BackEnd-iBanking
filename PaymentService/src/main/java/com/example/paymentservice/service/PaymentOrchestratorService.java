package com.example.paymentservice.service;

import com.example.paymentservice.client.AccountServiceClient;
import com.example.paymentservice.client.OtpNotificationServiceClient;
import com.example.paymentservice.client.TuitionServiceClient;
import com.example.paymentservice.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentOrchestratorService {

    private final AccountServiceClient accountClient;
    private final TuitionServiceClient tuitionClient;
    private final OtpNotificationServiceClient otpClient;

    // Không DB: lưu tạm các giao dịch đang giữ lock
    private final Map<String, PendingPayment> pending = new ConcurrentHashMap<>();

    public PaymentOrchestratorService(AccountServiceClient accountClient,
                                      TuitionServiceClient tuitionClient,
                                      OtpNotificationServiceClient otpClient) {
        this.accountClient = accountClient;
        this.tuitionClient = tuitionClient;
        this.otpClient = otpClient;
    }

    public PaymentInitResponse initiate(PaymentInitRequest req) {
        String paymentId = UUID.randomUUID().toString();

        Boolean studentOk = tuitionClient.studentExists(req.getMssv()).block();
        if (Boolean.FALSE.equals(studentOk)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MSSV không tồn tại");
        }

        Long balance = accountClient.getBalance(req.getUserId()).block();
        if (balance == null || balance < req.getAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số dư không đủ");
        }

        boolean userLocked = false;
        boolean studentLocked = false;
        try {
            userLocked = Boolean.TRUE.equals(accountClient.lockUser(req.getUserId()).block());
            if (!userLocked) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Không lock được user");
            }

            studentLocked = Boolean.TRUE.equals(tuitionClient.lockStudent(req.getMssv()).block());
            if (!studentLocked) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Không lock được MSSV");
            }

            Boolean otpCreated = otpClient.createOtp(paymentId, req.getUserId()).block();
            if (!Boolean.TRUE.equals(otpCreated)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không tạo được OTP");
            }

            pending.put(paymentId, new PendingPayment(req.getUserId(), req.getMssv(), req.getAmount()));
            return PaymentInitResponse.builder()
                    .paymentId(paymentId)
                    .otpRequired(true)
                    .build();

        } catch (ResponseStatusException e) {
            // 409 khi lock fail theo yêu cầu
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Lỗi gọi service phụ thuộc", e);
        }
        // Ghi chú: giữ lock cho đến khi confirm xong (hoặc có cơ chế timeout riêng)
    }

    public PaymentConfirmResponse confirm(PaymentConfirmRequest req) {
        // Theo spec: chỉ có OTP. Phải tra paymentId từ OTP service.
        String paymentId = otpClient.verifyOtp(req.getOtp()).block();
        if (paymentId == null || paymentId.isEmpty()) {
            // OTP sai → trả failed (200)
            return PaymentConfirmResponse.builder()
                    .status("failed")
                    .transactionId(null)
                    .build();
        }

        PendingPayment pp = pending.get(paymentId);
        if (pp == null) {
            // OTP hợp lệ nhưng pending không còn → xem như failed
            return PaymentConfirmResponse.builder()
                    .status("failed")
                    .transactionId(null)
                    .build();
        }

        try {
            Boolean balanceOk = accountClient.updateBalance(pp.userId, -pp.amount).block();
            if (!Boolean.TRUE.equals(balanceOk)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cập nhật số dư thất bại");
            }

            Boolean tuitionOk = tuitionClient.updateStatusPaid(pp.mssv, paymentId).block();
            if (!Boolean.TRUE.equals(tuitionOk)) {
                // rollback sơ bộ
                accountClient.updateBalance(pp.userId, pp.amount).block();
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cập nhật trạng thái học phí thất bại");
            }

            String txId = accountClient.saveTransaction(pp.userId, paymentId, pp.amount).block();
            pending.remove(paymentId);

            return PaymentConfirmResponse.builder()
                    .status("success")
                    .transactionId(txId != null ? txId : UUID.randomUUID().toString())
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Lỗi gọi service phụ thuộc", e);
        } finally {
            try { accountClient.unlockUser(pp.userId).block(); } catch (Exception ignored) {}
            try { tuitionClient.unlockStudent(pp.mssv).block(); } catch (Exception ignored) {}
        }
    }

    private static class PendingPayment {
        private final Long userId;
        private final String mssv;
        private final Long amount;

        private PendingPayment(Long userId, String mssv, Long amount) {
            this.userId = userId;
            this.mssv = mssv;
            this.amount = amount;
        }
    }
}