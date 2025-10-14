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
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_accounts_userId ON accounts(userId);


-- Seed data for accounts
-- INSERT INTO accounts (accountNumber, userId, balance) VALUES
--     (1000000001, 1, 100000000.00),
--     (1000000002, 2, 900000000.00),
--     (1000000003, 3, 999999999.00);