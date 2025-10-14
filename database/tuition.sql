-- Database: tuition_db
CREATE DATABASE IF NOT EXISTS tuition_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tuition_db;

-- Table: students
CREATE TABLE students (
    mssv VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    tuitionFee DECIMAL(15,2) NOT NULL CHECK (tuitionFee >= 0),
    status VARCHAR(50) NOT NULL DEFAULT 'Chưa thanh toán'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_students_status ON students(status);

-- Dữ liệu mẫu
-- INSERT INTO students (mssv, full_name, tuitionFee, status) VALUES
-- ('52300051', 'Phan Dinh Phu', 100000.00, 'Chưa thanh toán'),
-- ('52300056', 'Nguyen Duy Quy', 200000.00, 'Chưa thanh toán'),
-- ('52300055', 'Ngo Xuan Quang', 200000.00, 'Chưa thanh toán');