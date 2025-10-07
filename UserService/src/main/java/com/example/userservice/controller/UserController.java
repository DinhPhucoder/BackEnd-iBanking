package com.example.userservice.controller;

import java.util.Map;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigInteger;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable BigInteger id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(Map.of(
                        "userId", user.getUserId(),
                        "full_name", user.getFullName(),
                        "phone", user.getPhone(),
                        "email", user.getEmail()
                )))
                .orElse(ResponseEntity.notFound().build());
    }



}
