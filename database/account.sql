-- Database: account_db
CREATE DATABASE IF NOT EXISTS account_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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


-- Seed data for accounts
-- INSERT INTO accounts (accountNumber, userId, balance) VALUES
--     (1000000001, 1, 10000000.00),
--     (1000000002, 2, 7500000.00),
--     (1000000003, 3, 5000000.00);

-- Seed data for transactions
-- INSERT INTO transactions (transactionId, userId, mssv, amount, status, type, description) VALUES
--     ('tx-11111111-1111-1111-1111-111111111111', 1, 'S001', 2000000.00, 'SUCCESS', 'Thanh toán học phí', 'Thanh toán học phí S001'),
--     ('tx-22222222-2222-2222-2222-222222222222', 2, 'S002', 1500000.00, 'FAILED', 'Thanh toán học phí', 'Thanh toán thất bại do thiếu tiền'),
--     ('tx-33333333-3333-3333-3333-333333333333', 3, 'S003', 1000000.00, 'PENDING', 'Thanh toán học phí', 'Khởi tạo giao dịch');