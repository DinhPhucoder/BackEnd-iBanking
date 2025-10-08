package com.example.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.service.AuthService;
import java.math.BigInteger;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Validate input: username non-empty (<= 50), password non-empty (>= 8)
        String username = request != null ? request.getUsername() : null;
        String password = request != null ? request.getPassword() : null;

        boolean isUsernameEmpty = username == null || username.trim().isEmpty();
        boolean isPasswordEmpty = password == null || password.trim().isEmpty();
        if (isUsernameEmpty || isPasswordEmpty) {
            ErrorResponse error = new ErrorResponse();
            error.setError("Username or password cannot be empty");
            error.setCode(400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (username.length() > 50 || password.length() < 8) {
            ErrorResponse error = new ErrorResponse();
            error.setError("Invalid username or password");
            error.setCode(401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            BigInteger userId = authService.getUserId(username, password);
            LoginResponse resp = new LoginResponse();
            resp.setUserId(userId);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException ex) {
            ErrorResponse error = new ErrorResponse();
            error.setError("Invalid username or password");
            error.setCode(401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }



    @Data
    public static class LoginRequest {
        private String username;  // Tên đăng nhập
        private String password;  // Mật khẩu
    }


    @Data
    public static class LoginResponse {
        private BigInteger userId;
    }

    @Data
    public static class ErrorResponse {
        private String error;
        private int code;
    }
}
