

-- Table: otp
CREATE TABLE otp (
    otpID BIGINT AUTO_INCREMENT PRIMARY KEY,
    otpCode VARCHAR(10) NOT NULL,
    userId BIGINT NOT NULL,  -- REF đến user_db.user
    paymentReference VARCHAR(255),  -- Liên kết với transaction
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expiresAt TIMESTAMP NOT NULL CHECK (expiresAt > createdAt),
    status VARCHAR(50) DEFAULT 'Chưa sử dụng' NOT NULL
        CHECK (status IN ('Chưa sử dụng', 'Đã sử dụng', 'Hết hạn')),
    
    -- KHÔNG có FK vì cross-database, chỉ là reference
    -- FOREIGN KEY (userId) REFERENCES user_db.users(id) ON DELETE CASCADE,
    
    INDEX idx_otp_userId (userId),
    INDEX idx_otp_reference (paymentReference),
    INDEX idx_otp_expires (expiresAt),
    INDEX idx_otp_status (status),
    
    -- Unique constraint cho active OTP
    UNIQUE KEY uk_active_otp (userId, otpCode, status) 
        WHERE status = 'Chưa sử dụng' AND expiresAt > CURRENT_TIMESTAMP
)

-- Event scheduler để expire OTP
DELIMITER $$
CREATE EVENT IF NOT EXISTS expire_otp_event
ON SCHEDULE EVERY 1 MINUTE
DO
BEGIN
    UPDATE otp 
    SET status = 'Hết hạn' 
    WHERE status = 'Chưa sử dụng' AND expiresAt < NOW();
END$$
DELIMITER ;

SET GLOBAL event_scheduler = ON;

-- Sample data
INSERT INTO otp (otpCode, userId, paymentReference, expiresAt, status) VALUES
('123456', 1, 'TX001', DATE_ADD(NOW(), INTERVAL 5 MINUTE), 'Chưa sử dụng'),
('789012', 2, 'TX002', DATE_ADD(NOW(), INTERVAL 3 MINUTE), 'Chưa sử dụng');
