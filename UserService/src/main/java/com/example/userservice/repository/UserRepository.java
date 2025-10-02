package com.example.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.userservice.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    //Optional<User> findByUsernameAndPassword(String username, String password);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}


