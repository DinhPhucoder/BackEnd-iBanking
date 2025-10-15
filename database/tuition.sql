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
-- INSERT INTO `students` (`mssv`, `full_name`, `tuitionFee`, `status`) VALUES
-- ('1', 'Saka', 20000000, 'Chưa thanh toán'),
-- ('2', 'Jesus', 20000000, 'Chưa thanh toán'),
-- ('3', 'Eze', 20000000.00, 'Chưa thanh toán'),
-- ('52300040', 'Trần Tuấn Kiệt', 1400000.00, 'Chưa thanh toán'),
-- ('52300041', 'Trần Thanh Liêm', 11000000.00, 'Chưa thanh toán'),
-- ('52300042', 'Phạm Tiến Lực', 6000000.00, 'Chưa thanh toán'),
-- ('52300051', 'Phan Đình Phú', 10000000.00, 'Chưa thanh toán'),
-- ('52300054', 'Nguyễn Trần Minh Quân', 20000000.00, 'Chưa thanh toán'),
-- ('52300055', 'Ngô Xuân Quang', 2000000.00, 'Chưa thanh toán'),
-- ('52300056', 'Nguyễn Duy Quý', 2000000.00, 'Chưa thanh toán');