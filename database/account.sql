CREATE DATABASE IF NOT EXISTS account_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE account_db;

CREATE TABLE IF NOT EXISTS accounts (
    accountNumber BIGINT PRIMARY KEY,
    userId BIGINT,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_accounts_user FOREIGN KEY (userId)
        REFERENCES user_db.users(userID) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_accounts_userId ON accounts(userId);

-- Bảng transactions
CREATE TABLE IF NOT EXISTS transactions (
    transactionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NOT NULL,          -- FK đến accounts.userId
    mssv VARCHAR(20) NOT NULL UNIQUE,
    amount DECIMAL(15,2) NOT NULL,
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    `type` VARCHAR(50) NOT NULL DEFAULT 'Thanh toán khác',
    description VARCHAR(255),
    reference_id VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT fk_transactions_accounts FOREIGN KEY (userId)
        REFERENCES accounts(userId) ON DELETE CASCADE,
    INDEX idx_payer_timestamp (userId, `timestamp`),
    INDEX idx_status (status),
    INDEX idx_reference_id (reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CHÈN DỮ LIỆU MẪU: tạo 1 user trong user_db và 1 account (để không bị lỗi FK khi chèn transaction)
INSERT INTO user_db.users (username, password, fullName, email, phone)
VALUES ('testuser', 'pass', 'Test User', 'test@example.com', '0123456789');
INSERT INTO user_db.users (username, password, fullName, email, phone)
VALUES ('phu', '123', 'Phan Dinh Phu', 'phu@gmail.com', '0981234567');
-- Lưu ý: chuyển về account_db trước khi chèn tài khoản
USE account_db;
INSERT INTO accounts (accountNumber, userId, balance) VALUES (1000001, 1, 500000.00);

-- Chèn transaction mẫu
INSERT INTO transactions (userId, mssv, amount, status, `type`, reference_id) VALUES
(1, 'MSSV001', 1000000.00, 'SUCCESS', 'Thanh toán học phí', 'REF001');