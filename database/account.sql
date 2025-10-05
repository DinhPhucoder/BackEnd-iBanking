-- Database: account_db
CREATE DATABASE IF NOT EXISTS account_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE account_db;

-- Table: accounts
CREATE TABLE accounts (
    accountNumber BIGINT PRIMARY KEY,
    userId BIGINT , 
    balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Account mẫu cho user
INSERT INTO accounts (accountNumber, userId, balance) VALUES
(123456789, 1, 2350000.00);

-- Table: transactions
CREATE TABLE transactions (
    transactionId BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NOT NULL,  
    mssv VARCHAR(20) NOT NULL, 
    amount DECIMAL(15,2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    type VARCHAR(50) NOT NULL
        CHECK (type IN ('Thanh toán học phí', 'Nạp tiền', 'Thanh toán khác')),
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_accounts_userId ON accounts(userId);

-- Transaction mẫu (có thể chỉ định transactionId hoặc để auto)
INSERT INTO transactions (transactionId, userId, mssv, amount, status, type, description) VALUES
(1001, 1, 'MSSV001', 1000000.00, 'SUCCESS', 'Nạp tiền', 'Nạp tiền vào tài khoản'),
(1002, 1, 'MSSV001', -500000.00, 'SUCCESS', 'Thanh toán học phí', 'Thanh toán học phí HK1-2025'),
(1003, 1, 'MSSV001', -50000.00, 'SUCCESS', 'Thanh toán khác', 'Mua sách giáo khoa'),
(1004, 1, 'MSSV001', 2000000.00, 'SUCCESS', 'Nạp tiền', 'Nhận tiền từ gia đình'),
(1005, 1, 'MSSV001', -100000.00, 'SUCCESS', 'Thanh toán khác', 'Mua đồ dùng học tập');