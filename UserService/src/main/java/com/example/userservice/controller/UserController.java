package com.example.userservice.controller;

import java.util.Map;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
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

	@GetMapping("/{userId}")
	public ResponseEntity<?> getUserById(@PathVariable("userId") BigInteger userId) {
		// Validate: userId must be non-negative integer
		if (userId == null || userId.compareTo(BigInteger.ZERO) < 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
				"error", "Invalid userId",
				"code", 400
			));
		}

		return userRepository.findById(userId)
				.map(user -> ResponseEntity.ok(Map.of(
					"userId", user.getUserId(),
					"fullName", user.getFullName(),
					"phone", user.getPhone(),
					"email", user.getEmail()
				)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
					"error", "User not found",
					"code", 404
				)));
	}



}
