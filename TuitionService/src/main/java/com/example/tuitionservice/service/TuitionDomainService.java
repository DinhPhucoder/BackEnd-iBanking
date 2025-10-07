package com.example.tuitionservice.service;

import com.example.tuitionservice.dto.*;
import com.example.tuitionservice.model.Student;
//import com.example.tuitionservice.model.Payment;
import com.example.tuitionservice.repository.StudentRepository;
//import com.example.tuitionservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TuitionDomainService {
    private final StudentRepository studentRepository;
//    private final PaymentRepository paymentRepository;

    public TuitionDomainService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
//        this.paymentRepository = paymentRepository;
    }

    // ========== Lấy thông tin sinh viên ==========
    public Optional<Student> getStudent(String mssv) {
        return studentRepository.findByStudentId(mssv);
    }

    // ========== Lấy lịch sử thanh toán ==========
//    public ArrayList<Payment> getPaymentHistory(String mssv) {
//        return new ArrayList<>(
//                paymentRepository.findByMssvAndStatusOrderByTimestampDesc(mssv, "SUCCESS")
//        );
//    }

    // ========== Lưu giao dịch thanh toán ==========
//    public Payment savePayment(PaymentRequest req) {
//        Payment payment = new Payment();
//        // backend tự sinh id (UUID)
//        payment.setId(java.util.UUID.randomUUID().toString());
//        payment.setMssv(req.getMssv());
//        payment.setAmount(req.getAmount());
//        payment.setDescription(req.getDescription());
//        // Status mặc định là "PENDING" (có thể @PrePersist trong entity)
//        return paymentRepository.save(payment);
//    }

    // ========== Cập nhật học phí (trừ/hoàn tiền) ==========
    @Transactional
    public Student updateTuition(String mssv, BigDecimal amount) {
        Student student = studentRepository.findByStudentId(mssv)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        BigDecimal newFee = student.getTuitionFee().add(amount);
        if (newFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid amount or insufficient tuition balance");
        }
        student.setTuitionFee(newFee);
        return studentRepository.save(student);
    }

    // ========== Thanh toán học phí theo PaymentService ==========
    @Transactional
    public Student payTuition(String mssv, String transactionId, BigDecimal amount) {
        if (transactionId == null) {
            throw new IllegalArgumentException("transactionId is required");
        }
        Student student = studentRepository.findByStudentId(mssv)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (amount == null) {
            throw new IllegalArgumentException("Invalid amount or tuition already paid");
        }

        // amount must be negative and match current tuitionFee
        if (amount.compareTo(BigDecimal.ZERO) >= 0
                || amount.abs().compareTo(student.getTuitionFee()) != 0) {
            throw new IllegalArgumentException("Invalid amount or tuition already paid");
        }

        // Set paid and zero out tuition fee
        student.setTuitionFee(BigDecimal.ZERO);
        student.setStatus("paid");
        return studentRepository.save(student);
    }
}
