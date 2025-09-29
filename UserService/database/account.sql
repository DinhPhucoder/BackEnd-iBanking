-- Database: account_db
CREATE DATABASE IF NOT EXISTS account_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE account_db;

-- Table: accounts
CREATE TABLE accounts (
    accountNumber BIGINT PRIMARY KEY,  
    userId BIGINT ,  -- 1:1 với users.id
    balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL CHECK (balance >= 0),
    FOREIGN KEY (userId) REFERENCES user_db.users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: transactions
CREATE TABLE transactions (
    transactionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NOT NULL,  -- FK đến accounts.userId
    mssv VARCHAR(20) NOT NULL UNIQUE,  -- 1:1 với students.mssv
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    type VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('Thanh toán học phí', 'Nạp tiền', 'Thanh toán khác')),
    description VARCHAR(255),
    reference_id VARCHAR(255) UNIQUE NOT NULL,
    
    -- FK constraints
    FOREIGN KEY (userId) REFERENCES accounts(userId) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_payer_timestamp (userId, timestamp DESC),
    INDEX idx_status (status),
    INDEX idx_reference_id (reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_accounts_userId ON accounts(userId);

-- Transaction mẫu
INSERT INTO transactions (userId, mssv, amount, status) VALUES
(1, 'MSSV001', 1000000.00, 'SUCCESS');