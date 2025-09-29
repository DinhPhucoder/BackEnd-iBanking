-- Table: students
CREATE TABLE students (
    mssv VARCHAR(20) PRIMARY KEY,
    fullName VARCHAR(100) NOT NULL,
    tuitionFee DECIMAL(15,2) NOT NULL CHECK (tuitionFee > 0),
    status VARCHAR(50) NOT NULL DEFAULT 'Chưa thanh toán'
        CHECK (status IN ('Chưa thanh toán', 'Đã thanh toán', 'Đang xử lý')),
    UserId BIGINT,  
   
)

-- Indexes
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_parent ON students(parentUserId);

-- Dữ liệu mẫu
INSERT INTO students (mssv, fullName, tuitionFee, status) VALUES
('MSSV001', 'Lê Văn C', 5000000.00, 'Chưa thanh toán'),
('MSSV002', 'Phạm Thị D', 4500000.00, 'Chưa thanh toán'),
('MSSV003', 'Hoàng Văn E', 6000000.00, 'Đã thanh toán');