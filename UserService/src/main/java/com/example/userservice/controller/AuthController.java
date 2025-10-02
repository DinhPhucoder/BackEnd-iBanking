package com.example.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.service.AuthService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Long userID = authService.getUserId(request.getUsername(), request.getPassword());
//        String fullName = authService.getFullName(request.getUsername(), request.getPassword());
        LoginResponse resp = new LoginResponse();
        resp.setUserID(userID);
//        resp.setFullName(fullName);
        return ResponseEntity.ok(resp);
    }



    @Data
    public static class LoginRequest {
        private String username;  // Tên đăng nhập
        private String password;  // Mật khẩu
    }


    @Data
    public static class LoginResponse {
        private Long userID;     // hoặc String userId nếu userId bên DB bạn lưu kiểu chuỗi
//        private String fullName; // nếu bạn muốn trả về thêm tên user
    }


}
