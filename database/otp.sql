-- Database: otp_db
CREATE DATABASE IF NOT EXISTS otp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otp_db;

-- Table: otp
CREATE TABLE otp (
    otpID VARCHAR(50) PRIMARY KEY,
    otpCode VARCHAR(10) NOT NULL,
    userId BIGINT NOT NULL,  
    paymentReference VARCHAR(255),  
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expiresAt TIMESTAMP NOT NULL CHECK (expiresAt > createdAt),
    status VARCHAR(50) DEFAULT 'Chưa sử dụng' NOT NULL
        CHECK (status IN ('Chưa sử dụng', 'Đã sử dụng', 'Hết hạn')),
    
    
    INDEX idx_otp_userId (userId),
    INDEX idx_otp_reference (paymentReference),
    INDEX idx_otp_expires (expiresAt),
    INDEX idx_otp_status (status),
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO otp (otpCode, userId, paymentReference, expiresAt, status) VALUES
('123456', 1, 'TX001', DATE_ADD(NOW(), INTERVAL 5 MINUTE), 'Chưa sử dụng'),
('789012', 2, 'TX002', DATE_ADD(NOW(), INTERVAL 3 MINUTE), 'Chưa sử dụng');
