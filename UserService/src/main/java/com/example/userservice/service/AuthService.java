package com.example.userservice.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * 🧠 AUTH SERVICE - LỚP XỬ LÝ LOGIC ĐĂNG NHẬP VÀ XÁC THỰC
 * 
 * @Service: Đánh dấu đây là Service layer (Business Logic)
 * @RequiredArgsConstructor: Lombok tự động tạo constructor với các field final
 * 
 * Service layer chứa:
 * - Business logic (logic nghiệp vụ)
 * - Validation (kiểm tra dữ liệu)
 * - Orchestration (điều phối giữa các component)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // 🗄️ DEPENDENCY INJECTION - Spring tự động inject các dependency này
    private final UserRepository userRepository;    // Truy cập database
    private final PasswordEncoder passwordEncoder; // Mã hóa password (chưa sử dụng)
    private final JwtService jwtService;           // Tạo JWT token

    /**
     * 🔐 PHƯƠNG THỨC LOGIN - XỬ LÝ ĐĂNG NHẬP USER
     * 
     * @param username: Tên đăng nhập từ client
     * @param rawPassword: Mật khẩu thô từ client
     * @return String: JWT token nếu đăng nhập thành công
     * @throws RuntimeException: Nếu username/password không đúng
     * 
     * Luồng xử lý:
     * 1. Tìm user trong database theo username
     * 2. Kiểm tra password có đúng không
     * 3. Nếu đúng: Tạo JWT token với thông tin user
     * 4. Nếu sai: Throw exception
     */
    public String login(String username, String rawPassword) {
        // 🔍 BƯỚC 1: TÌM USER TRONG DATABASE
        // findByUsername() trả về Optional<User>
        // orElseThrow(): Nếu không tìm thấy user, throw exception
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        // 🔐 BƯỚC 2: KIỂM TRA PASSWORD
        // Hiện tại so sánh trực tiếp (plain text)
        // Trong thực tế nên dùng: passwordEncoder.matches(rawPassword, user.getPassword())
        if (!rawPassword.equals(user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        // 🎫 BƯỚC 3: TẠO JWT TOKEN VỚI THÔNG TIN USER
        // Claims = Thông tin bổ sung trong JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());      // ID của user
        claims.put("fullName", user.getFullName());   // Tên đầy đủ
        claims.put("email", user.getEmail());         // Email
        
        // Tạo và trả về JWT token
        return jwtService.generateToken(user.getUsername(), claims);
    }
}


