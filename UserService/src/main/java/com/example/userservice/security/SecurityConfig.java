package com.example.userservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 🛡️ SECURITY CONFIG - CẤU HÌNH BẢO MẬT CHO ỨNG DỤNG
 * 
 * @Configuration: Đánh dấu đây là class cấu hình Spring
 * @EnableWebSecurity: Kích hoạt Spring Security cho web application
 * 
 * Chức năng chính:
 * - Cấu hình authentication (xác thực)
 * - Cấu hình authorization (phân quyền)
 * - Cấu hình CORS (Cross-Origin Resource Sharing)
 * - Cấu hình CSRF protection
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 🔐 PASSWORD ENCODER BEAN - CẤU HÌNH MÃ HÓA PASSWORD
     * 
     * @Bean: Tạo bean PasswordEncoder để Spring có thể inject
     * 
     * BCryptPasswordEncoder:
     * - Thuật toán mã hóa mạnh (bcrypt)
     * - Tự động tạo salt (muối) để tăng bảo mật
     * - Mỗi lần mã hóa cùng 1 password sẽ cho kết quả khác nhau
     * 
     * Ví dụ:
     * - Password: "123456"
     * - Encoded: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 🔒 SECURITY FILTER CHAIN - CẤU HÌNH CHÍNH CỦA SECURITY
     * 
     * @param http: HttpSecurity object để cấu hình
     * @return SecurityFilterChain: Chuỗi filter xử lý security
     * 
     * Các cấu hình chính:
     * 1. CORS: Cho phép request từ domain khác
     * 2. CSRF: Tắt CSRF protection (vì dùng JWT)
     * 3. Session: Stateless (không lưu session)
     * 4. Authorization: Phân quyền truy cập endpoints
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 🌐 CORS CONFIGURATION - Cho phép request từ domain khác
            .cors(Customizer.withDefaults())
            
            // 🚫 CSRF PROTECTION - Tắt CSRF vì dùng JWT
            .csrf(csrf -> csrf.disable())
            
            // 📱 SESSION MANAGEMENT - Stateless (không lưu session)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 🔐 AUTHORIZATION - Phân quyền truy cập endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // Cho phép truy cập /api/auth/* không cần xác thực
                .anyRequest().authenticated()                 // Các endpoint khác cần xác thực
            );
        
        return http.build();
    }

    /**
     * 🌐 CORS CONFIGURATION SOURCE - CẤU HÌNH CHI TIẾT CORS
     * 
     * CORS (Cross-Origin Resource Sharing):
     * - Cho phép web app từ domain này gọi API từ domain khác
     * - Ví dụ: Android app (localhost) gọi API (192.168.1.9:8081)
     * 
     * @return CorsConfigurationSource: Cấu hình CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 🌍 ALLOWED ORIGINS - Các domain được phép gọi API
        configuration.setAllowedOrigins(List.of("*"));  // "*" = cho phép tất cả domain
        
        // 📡 ALLOWED METHODS - Các HTTP methods được phép
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 📋 ALLOWED HEADERS - Các header được phép gửi
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        
        // 📤 EXPOSED HEADERS - Các header được phép trả về
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // 🔧 REGISTER CORS CONFIGURATION
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Áp dụng cho tất cả endpoints
        
        return source;
    }
}


