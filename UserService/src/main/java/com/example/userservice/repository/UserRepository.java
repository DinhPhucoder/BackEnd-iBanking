package com.example.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.userservice.model.User;

/**
 * 🗄️ USER REPOSITORY - LỚP TRUY CẬP DATABASE CHO USER
 * 
 * JpaRepository<User, Long>:
 * - User: Entity type (kiểu dữ liệu)
 * - Long: Kiểu dữ liệu của Primary Key (userId)
 * 
 * JpaRepository cung cấp sẵn các method:
 * - save(User user): Lưu user vào database
 * - findById(Long id): Tìm user theo ID
 * - findAll(): Lấy tất cả user
 * - deleteById(Long id): Xóa user theo ID
 * - count(): Đếm số lượng user
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 🔍 TÌM USER THEO USERNAME
     * 
     * @param username: Tên đăng nhập cần tìm
     * @return Optional<User>: 
     * - Nếu tìm thấy: Optional chứa User object
     * - Nếu không tìm thấy: Optional.empty()
     * 
     * Spring Data JPA tự động tạo implementation:
     * SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);
    
    /**
     * ✅ KIỂM TRA USERNAME ĐÃ TỒN TẠI CHƯA
     * 
     * @param username: Tên đăng nhập cần kiểm tra
     * @return boolean: 
     * - true: Username đã tồn tại
     * - false: Username chưa tồn tại
     * 
     * Spring Data JPA tự động tạo implementation:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
    
    /**
     * ✅ KIỂM TRA EMAIL ĐÃ TỒN TẠI CHƯA
     * 
     * @param email: Email cần kiểm tra
     * @return boolean:
     * - true: Email đã tồn tại
     * - false: Email chưa tồn tại
     * 
     * Spring Data JPA tự động tạo implementation:
     * SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}


