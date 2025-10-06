package com.example.userservice.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User login(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        String storedPassword = user.getPassword();

        // Hỗ trợ cả hai trường hợp: mật khẩu đã mã hóa (bcrypt) hoặc plain text
        boolean isBcrypt = storedPassword != null && storedPassword.startsWith("$2");
        boolean matched = isBcrypt
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : rawPassword != null && rawPassword.equals(storedPassword);

        if (!matched) return null;

        return user;
    }
}