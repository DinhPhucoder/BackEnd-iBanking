package com.example.tuitionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockRequest {
    private String mssv;
}