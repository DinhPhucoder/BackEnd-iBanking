package com.example.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📋 USER ENTITY - ĐỊNH NGHĨA CẤU TRÚC BẢNG USERS TRONG DATABASE
 * 
 * @Entity: Đánh dấu class này là một JPA Entity (tương ứng với 1 bảng trong DB)
 * @Table(name = "users"): Chỉ định tên bảng trong database là "users"
 * 
 * Lombok Annotations:
 * @Data: Tự động tạo getter, setter, toString, equals, hashCode
 * @NoArgsConstructor: Tạo constructor không tham số
 * @AllArgsConstructor: Tạo constructor với tất cả tham số
 * @Builder: Tạo pattern Builder để tạo object dễ dàng
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * 🆔 PRIMARY KEY - ID DUY NHẤT CỦA USER
     * 
     * @Id: Đánh dấu đây là primary key
     * @GeneratedValue(strategy = GenerationType.IDENTITY): 
     * - Tự động tăng ID (1, 2, 3, 4...)
     * - IDENTITY = AUTO_INCREMENT trong MySQL
     * @Column(name = "userID"): Tên cột trong database là "userID"
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Long userId;

    /**
     * 👤 USERNAME - TÊN ĐĂNG NHẬP
     * 
     * @Column(nullable = false): Không được null (bắt buộc)
     * @Column(unique = true): Phải duy nhất (không trùng lặp)
     * @Column(length = 100): Độ dài tối đa 100 ký tự
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * 🔐 PASSWORD - MẬT KHẨU
     * 
     * @Column(nullable = false): Không được null (bắt buộc)
     * @Column(length = 255): Độ dài tối đa 255 ký tự
     * 
     * Lưu ý: Hiện tại đang lưu plain text, trong thực tế nên mã hóa bằng BCrypt
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * 📝 FULL NAME - HỌ VÀ TÊN ĐẦY ĐỦ
     * 
     * @Column(nullable = false): Không được null (bắt buộc)
     * @Column(length = 150): Độ dài tối đa 150 ký tự
     */
    @Column(nullable = false, length = 150)
    private String fullName;

    /**
     * 📞 PHONE - SỐ ĐIỆN THOẠI
     * 
     * @Column(nullable = false): Không được null (bắt buộc)
     * @Column(length = 20): Độ dài tối đa 20 ký tự
     */
    @Column(nullable = false, length = 20)
    private String phone;

    /**
     * 📧 EMAIL - ĐỊA CHỈ EMAIL
     * 
     * @Column(nullable = false): Không được null (bắt buộc)
     * @Column(unique = true): Phải duy nhất (không trùng lặp)
     * @Column(length = 150): Độ dài tối đa 150 ký tự
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;
}


