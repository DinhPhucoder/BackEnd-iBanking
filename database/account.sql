-- Database: account_db
CREATE DATABASE IF NOT EXISTS account_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'account_user'@'%' IDENTIFIED BY 'account123';
GRANT ALL PRIVILEGES ON account_db.* TO 'account_user'@'%';
FLUSH PRIVILEGES;
USE account_db;

-- Table: accounts
CREATE TABLE accounts (
    accountNumber BIGINT PRIMARY KEY,
    userId BIGINT ,
    balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: transactions
CREATE TABLE transactions (
    transactionId VARCHAR(100) PRIMARY KEY,
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

-- Transaction mẫu
-- INSERT INTO transactions (userId, mssv, amount, status) VALUES
-- (1, 'MSSV001', 1000000.00, 'SUCCESS');