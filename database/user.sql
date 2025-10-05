-- user_db
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_db;

CREATE TABLE users (
    userID BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL
);

-- User mẫu
INSERT INTO users (userID, username, password, full_name, email, phone) VALUES
(1, 'testuser', '123456', 'Nguyen Van A', 'test@example.com', '0123456789'),
(2, 'admin', 'admin123', 'Admin User', 'admin@example.com', '0987654321');