package com.example.userservice.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    // 🗄️ DEPENDENCY INJECTION - Spring tự động inject các dependency này
    private final UserRepository userRepository;    // Truy cập database
    private final PasswordEncoder passwordEncoder; // Mã hóa password (chưa sử dụng)
    private final JwtService jwtService;           // Tạo JWT token

    public String login(String username, String rawPassword) {
        // 🔍 BƯỚC 1: TÌM USER TRONG DATABASE
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        // 🔐 BƯỚC 2: KIỂM TRA PASSWORD
        if (!rawPassword.equals(user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        // 🎫 BƯỚC 3: TẠO JWT TOKEN VỚI THÔNG TIN USER
        Map<String, Object> claims = new HashMap<>();
        claims.put("userID", user.getUserId());      // ID của user
        claims.put("fullName", user.getFullName());   // Tên đầy đủ
        claims.put("email", user.getEmail());         // Email
        
        // Tạo và trả về JWT token
        return jwtService.generateToken(user.getUsername(), claims);
    }


    public Long getUserId(String username, String password) {
        // query DB để lấy userId
        return userRepository.findByUsername(username)
                .map(User::getUserId)
                .orElse(null);
    }
    public String getFullName(String username, String password) {
        // query DB để lấy fullName
        return userRepository.findByUsername(username)
                .map(User::getFullName)
                .orElse(null);
    }


}


