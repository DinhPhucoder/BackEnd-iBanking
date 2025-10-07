package com.example.tuitionservice.controller;

import com.example.tuitionservice.model.Student;
import com.example.tuitionservice.dto.*;
import com.example.tuitionservice.service.RedisLockService;
import com.example.tuitionservice.service.TuitionDomainService;
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
    private final TuitionDomainService tuitionDomainService;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
        this.tuitionDomainService = null; // will be autowired via field injection below
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
    @Autowired
    private TuitionDomainService studentService; // existing code references studentService

    @PutMapping("/{mssv}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable("mssv") String mssv,
                                                            @RequestBody StatusRequest req) {
        if (mssv == null || mssv.trim().isEmpty() || req == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(400, "Invalid request"));
        }

        var opt = studentRepository.findById(mssv);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Student not found"));
        }

        if (req.getTransactionId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error(400, "Invalid amount or tuition already paid"));
        }

        try {
            var updated = studentService.payTuition(mssv, req.getTransactionId(), req.getAmount());
            Map<String, Object> res = new HashMap<>();
            res.put("mssv", updated.getStudentId());
            res.put("status", "paid");
            res.put("tuitionFee", java.math.BigDecimal.ZERO);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("Student not found".equals(msg)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Student not found"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error(400, "Invalid amount or tuition already paid"));
        }
    }
}

