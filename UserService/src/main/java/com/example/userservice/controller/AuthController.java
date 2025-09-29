package com.example.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.service.AuthService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 🎮 AUTH CONTROLLER - LỚP XỬ LÝ HTTP REQUESTS CHO AUTHENTICATION
 * 
 * @RestController: Đánh dấu đây là REST API Controller
 * - Tự động convert object thành JSON response
 * - Xử lý HTTP requests/responses
 * 
 * @RequestMapping("/api/auth"): Base URL cho tất cả endpoints trong controller này
 * - Ví dụ: /api/auth/login, /api/auth/register, /api/auth/logout
 * 
 * @RequiredArgsConstructor: Lombok tự động tạo constructor với các field final
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // 🧠 DEPENDENCY INJECTION - Spring tự động inject AuthService
    private final AuthService authService;

    /**
     * 🔐 ENDPOINT LOGIN - XỬ LÝ ĐĂNG NHẬP USER
     * 
     * @PostMapping("/login"): 
     * - HTTP Method: POST
     * - URL: /api/auth/login
     * - Full URL: http://localhost:8081/api/auth/login
     * 
     * @RequestBody LoginRequest request:
     * - Tự động convert JSON từ request body thành LoginRequest object
     * - Ví dụ JSON: {"username": "testuser", "password": "123456"}
     * 
     * @return ResponseEntity<TokenResponse>:
     * - HTTP Status: 200 OK (nếu thành công)
     * - Body: {"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
     * 
     * Luồng xử lý:
     * 1. Nhận JSON từ client (Android app)
     * 2. Gọi AuthService.login() để xử lý logic
     * 3. Nhận JWT token từ service
     * 4. Trả về token cho client
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        // 🧠 GỌI SERVICE ĐỂ XỬ LÝ LOGIC ĐĂNG NHẬP
        String token = authService.login(request.getUsername(), request.getPassword());
        
        // 📦 TẠO RESPONSE OBJECT
        TokenResponse resp = new TokenResponse();
        resp.setToken(token);
        
        // ✅ TRẢ VỀ HTTP 200 OK VỚI TOKEN
        return ResponseEntity.ok(resp);
    }

    /**
     * 📥 LOGIN REQUEST - CLASS ĐỊNH NGHĨA CẤU TRÚC REQUEST
     * 
     * @Data: Lombok tự động tạo getter, setter, toString, equals, hashCode
     * 
     * JSON request sẽ có dạng:
     * {
     *   "username": "testuser",
     *   "password": "123456"
     * }
     */
    @Data
    public static class LoginRequest {
        private String username;  // Tên đăng nhập
        private String password;  // Mật khẩu
    }

    /**
     * 📤 TOKEN RESPONSE - CLASS ĐỊNH NGHĨA CẤU TRÚC RESPONSE
     * 
     * JSON response sẽ có dạng:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYz..."
     * }
     */
    @Data
    public static class TokenResponse {
        private String token;  // JWT token để xác thực các request sau
    }
}


