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
-- INSERT INTO users (username, password, full_name, email, phone) VALUES
-- ('phu', '123', 'Phan Dinh Phu', 'dinhphan1209@gmail.com', '0980000001'),
-- ('quy', '123', 'Nguyen Duy Quy', 'nguyenduyquy2401gmail.com', '0970000002'),
-- ('quang', '123', 'Ngo Xuan Quang', 'ngoxuanquang2005@gmail.com', '0900000003');