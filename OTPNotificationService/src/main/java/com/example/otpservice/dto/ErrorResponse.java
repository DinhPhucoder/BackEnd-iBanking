package com.example.otpservice.dto;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private int code;

//    public ErrorResponse(String error, int error) {
//        this.error = error;
//        this.code = code;
//    }

}


