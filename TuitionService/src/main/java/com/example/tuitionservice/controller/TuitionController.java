package com.example.tuitionservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/students")
public class TuitionController {

    @GetMapping("/{mssv}/exists")
    public ResponseEntity<Boolean> checkExists(@PathVariable String mssv) {
        // Mock: luôn coi là tồn tại để test flow PaymentService
        return ResponseEntity.ok(true);
    }

    @GetMapping("/{mssv}")
    public ResponseEntity<Map<String, Object>> getStudent(@PathVariable String mssv) {
        // Mock: Trả về thông tin sinh viên
        Map<String, Object> student = Map.of(
            "mssv", mssv,
            "name", "Student " + mssv,
            "status", "ACTIVE"
        );
        return ResponseEntity.ok(student);
    }

    @PostMapping("/{mssv}/lock")
    public ResponseEntity<Map<String, Object>> lockTuition(@PathVariable String mssv) {
        // Mock: Luôn lock thành công
        Map<String, Object> response = Map.of(
            "locked", true,
            "lockKey", "lock_" + mssv + "_" + System.currentTimeMillis()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{mssv}/unlock")
    public ResponseEntity<Map<String, Object>> unlockTuition(@PathVariable String mssv) {
        // Mock: Luôn unlock thành công
        Map<String, Object> response = Map.of("unlocked", true);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{mssv}/status")
    public ResponseEntity<Map<String, Object>> updateTuitionStatus(@PathVariable String mssv, @RequestBody Map<String, Object> request) {
        // Mock: Luôn cập nhật thành công
        Map<String, Object> response = Map.of("success", true);
        return ResponseEntity.ok(response);
    }
}
