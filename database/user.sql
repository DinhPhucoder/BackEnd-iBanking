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

-- Seed users
INSERT INTO users (username, password, full_name, email, phone) VALUES
('user1', '$2a$10$abcdefghijklmnopqrstuv', 'Nguyễn Văn A', 'user1@example.com', '0900000001'),
('user2', '$2a$10$abcdefghijklmnopqrstuv', 'Trần Thị B', 'user2@example.com', '0900000002'),
('user3', '$2a$10$abcdefghijklmnopqrstuv', 'Phạm Văn C', 'user3@example.com', '0900000003');