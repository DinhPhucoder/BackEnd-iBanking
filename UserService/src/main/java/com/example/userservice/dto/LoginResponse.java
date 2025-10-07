package com.example.userservice.dto;

import lombok.Data;
import java.math.BigInteger;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private BigInteger userID;
    private String email;
    private String fullName;
}
