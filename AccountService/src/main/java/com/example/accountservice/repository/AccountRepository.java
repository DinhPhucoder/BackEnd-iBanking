package com.example.accountservice.repository;

import com.example.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigInteger;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, BigInteger> {
    Optional<Account> findByUserId(BigInteger userId);
}


