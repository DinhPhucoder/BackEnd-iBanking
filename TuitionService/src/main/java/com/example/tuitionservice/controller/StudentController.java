package com.example.tuitionservice.controller;

import com.example.tuitionservice.model.Student;
import com.example.tuitionservice.dto.*;
import com.example.tuitionservice.service.RedisLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.example.tuitionservice.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // API: GET /students/{mssv}/tuition
    @GetMapping("/{mssv}/tuition")
    public ResponseEntity<Student> getTuitionByStudentId(@PathVariable String mssv) {
        return studentRepository.findById(mssv)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private RedisLockService redisLockService;

    @Autowired(required = false)
    // ============ LOCK ============
    @PostMapping("/{mssv}/lock")
    public ResponseEntity<?> lock(@PathVariable("mssv") String mssv) {
        if (mssv == null || mssv.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error(400, "Invalid mssv"));
        }
        String lockKey = "lock_" + mssv + "_" + System.currentTimeMillis();
        boolean locked = redisLockService.lockTuition(mssv, lockKey);

        if (!locked) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(error(409, "Tuition already locked"));
        }

        LockResponse response = new LockResponse();
        response.setLocked(true);
        response.setLockKey(lockKey);
        response.setExpiry(System.currentTimeMillis() + 120_000); // TTL 120s
        return ResponseEntity.ok(response);
    }

    // ============ UNLOCK ============
    @PostMapping("/{mssv}/unlock")
    public ResponseEntity<?> unlock(@PathVariable("mssv") String mssv,
                                    @RequestBody UnlockRequest request) {
        if (mssv == null || mssv.trim().isEmpty()
                || request == null
                || request.getMssv() == null
                || request.getLockKey() == null
                || request.getLockKey().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error(400, "Invalid mssv or lockKey"));
        }

        if (!mssv.equals(request.getMssv())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error(400, "MSSV mismatch"));
        }
        boolean unlocked = redisLockService.unlockTuition(mssv, request.getLockKey());
        if (!unlocked) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error(400, "Invalid mssv or lockKey"));
        }

        UnlockResponse response = new UnlockResponse();
        response.setUnlocked(true);
        return ResponseEntity.ok(response);
    }

    // ============ Kiểm tra trạng thái LOCK ============
    @GetMapping("/{mssv}/lock-status")
    public ResponseEntity<Map<String, Object>> getLockStatus(@PathVariable("mssv") String mssv) {
        boolean locked = redisLockService.isTuitionLocked(mssv);
        String currentKey = redisLockService.getLockKey(mssv);
        Map<String, Object> res = new HashMap<>();
        res.put("mssv", mssv);
        res.put("locked", locked);
        res.put("lockKey", currentKey);
        return ResponseEntity.ok(res);
    }

    // ============ Helper ============
    private Map<String, Object> error(int code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", message);
        map.put("code", code);
        return map;
    }

    // ============ Update status theo PaymentService ============
    @PutMapping("/{mssv}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable("mssv") String mssv,
                                                            @RequestBody Map<String, Object> req) {
        // Expect: { transactionId, mssv, amount }
        if (mssv == null || mssv.trim().isEmpty() || req == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid request"));
        }
        var opt = studentRepository.findById(mssv);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Student not found"));
        }
        java.math.BigDecimal amount = null;
        try {
            Object amt = req.get("amount");
            if (amt instanceof Number) {
                amount = new java.math.BigDecimal(((Number) amt).toString());
            } else if (amt instanceof String) {
                amount = new java.math.BigDecimal((String) amt);
            }
        } catch (Exception ignore) {}
        if (amount == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid amount"));
        }

        var student = opt.get();
        // Nếu amount âm có độ lớn bằng học phí, coi như thanh toán thành công
        if (amount.compareTo(java.math.BigDecimal.ZERO) < 0 && amount.abs().compareTo(student.getTuitionFee()) == 0) {
            student.setStatus("Đã thanh toán");
            studentRepository.save(student);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            return ResponseEntity.ok(res);
        }
        // Không khớp số tiền
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        return ResponseEntity.ok(res);
    }
}

