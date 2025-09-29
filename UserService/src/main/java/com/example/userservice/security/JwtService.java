package com.example.userservice.security;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * 🔐 JWT SERVICE - LỚP XỬ LÝ JWT TOKEN
 * 
 * JWT (JSON Web Token) là một chuẩn để tạo token xác thực:
 * - Header: Thuật toán mã hóa (HS256)
 * - Payload: Thông tin user (claims)
 * - Signature: Chữ ký để xác thực token
 * 
 * @Service: Đánh dấu đây là Service layer
 */
@Service
public class JwtService {

    /**
     * 🔑 JWT SECRET KEY - KHÓA BÍ MẬT ĐỂ MÃ HÓA TOKEN
     * 
     * @Value("${security.jwt.secret}"): Lấy giá trị từ application.properties
     * - security.jwt.secret=ZmFrZV9zZWNyZXRfYmFzZTY0X2tleV9taW5fMzJfY2hhcnM=
     * - Đây là Base64 encoded key
     */
    @Value("${security.jwt.secret}")
    private String jwtSecretBase64;

    /**
     * ⏰ TOKEN EXPIRATION TIME - THỜI GIAN HẾT HẠN CỦA TOKEN
     * 
     * @Value("${security.jwt.expiration-ms:3600000}"): 
     * - Lấy từ application.properties
     * - Default: 3600000ms = 1 giờ
     * - security.jwt.expiration-ms=3600000
     */
    @Value("${security.jwt.expiration-ms:3600000}")
    private long expirationMs;

    /**
     * 🔐 TẠO SIGNING KEY - CHUYỂN BASE64 SECRET THÀNH KEY
     * 
     * @return Key: Khóa để ký và xác thực JWT token
     * 
     * Quá trình:
     * 1. Decode Base64 string thành byte array
     * 2. Tạo HMAC-SHA256 key từ byte array
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 🎫 TẠO JWT TOKEN - PHƯƠNG THỨC CHÍNH ĐỂ TẠO TOKEN
     * 
     * @param subject: Chủ thể của token (thường là username)
     * @param claims: Thông tin bổ sung (userId, fullName, email...)
     * @return String: JWT token đã được mã hóa
     * 
     * Ví dụ token được tạo:
     * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsImZ1bGxOYW1lIjoiTmd1eWVuIFZhbiBBIiwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIiwic3ViIjoidGVzdHVzZXIiLCJpYXQiOjE2MzI4NzQ0MDAsImV4cCI6MTYzMjg3ODAwMH0.signature
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();                                    // Thời gian hiện tại
        Date expiry = new Date(now.getTime() + expirationMs);     // Thời gian hết hạn
        
        return Jwts.builder()
                .setClaims(claims)                    // Thêm claims (userId, fullName, email...)
                .setSubject(subject)                  // Set subject (username)
                .setIssuedAt(now)                     // Thời gian tạo token
                .setExpiration(expiry)                // Thời gian hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Ký token với HMAC-SHA256
                .compact();                          // Tạo token string
    }

    /**
     * 🔍 PARSE JWT TOKEN - GIẢI MÃ VÀ XÁC THỰC TOKEN
     * 
     * @param token: JWT token cần giải mã
     * @return Claims: Thông tin trong token (userId, fullName, email...)
     * @throws Exception: Nếu token không hợp lệ hoặc đã hết hạn
     * 
     * Sử dụng khi:
     * - Xác thực request từ client
     * - Lấy thông tin user từ token
     * - Kiểm tra token có hợp lệ không
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())       // Set key để xác thực
                .build()
                .parseClaimsJws(token)               // Parse và xác thực token
                .getBody();                          // Lấy claims từ token
    }
}


