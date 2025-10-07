package com.example.userservice.service;

import org.springframework.stereotype.Service;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;    // Truy cập database

    public Long getUserId(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
    }
}


