CREATE TABLE IF NOT EXISTS accounts (
    accountNumber BIGINT PRIMARY KEY,
    userId BIGINT NOT NULL UNIQUE,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00
);
CREATE TABLE IF NOT EXISTS transactions (
    transactionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NOT NULL,
    mssv VARCHAR(20) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_transactions_accounts FOREIGN KEY (userId)
        REFERENCES accounts(userId) ON DELETE CASCADE
);

-- seed
INSERT INTO accounts (accountNumber, userId, balance) VALUES (1000001, 1, 500000.00);

INSERT INTO transactions (userId, mssv, amount, status, `type`) VALUES
(1, 'MSSV001', 100000.00, 'SUCCESS', 'Thanh toán học phí');