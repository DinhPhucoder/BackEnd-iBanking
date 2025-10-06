# 👥 HƯỚNG DẪN CHO TEAM - DỰ ÁN IBANKING

## 🎯 TỔNG QUAN

Dự án iBanking sử dụng kiến trúc **Microservices** với **Docker** và 1 MySQL duy nhất (host `ibanking`) chứa 3 schema: `account_db`, `user_db`, `tuition_db`.

### 📊 Kiến trúc hệ thống:
```
┌─────────────────────────────────────────────────────────┐
│                MICROSERVICES + DATABASES               │
├─────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────┐  │
│  │               MySQL (ibanking:3306)               │  │
│  │  account_db   |   user_db   |   tuition_db        │  │
│  └───────────────────────────────────────────────────┘  │
│         │               │               │              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │AccountSvc   │  │UserSvc     │  │TuitionSvc   │     │
│  │Port 8082    │  │Port 8081  │  │Port 8084   │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
│                                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │PaymentSvc  │  │OTPSvc      │  │Redis       │     │
│  │Port 8083    │  │Port 8085  │  │Port 6379  │     │
│  │(No DB)      │  │(No DB)    │  │(Cache)    │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
```

## 🚀 CÁCH KHỞI ĐỘNG DỰ ÁN

### **Bước 1: Chuẩn bị môi trường**
1. **Cài đặt Docker Desktop** từ: https://www.docker.com/products/docker-desktop/
2. **Khởi động Docker Desktop** (chờ icon Docker chuyển sang màu xanh)
3. **Cài đặt Git** (nếu chưa có)

### **Bước 2: Clone và chạy dự án**
```bash
# Clone code
git clone [URL_REPO]
cd ibanking

# Chạy dự án
docker-compose up -d

# Kiểm tra trạng thái
docker ps
```

### **Bước 3: Kiểm tra thành công**
- ✅ **8 containers** đang chạy
- ✅ **3 databases** hoạt động
- ✅ **5 services** sẵn sàng

## 🌐 TRUY CẬP CÁC DỊCH VỤ

### **📊 DATABASE MANAGEMENT (phpMyAdmin)**

- **URL:** http://localhost:8080
- **Server/Host:** `ibanking`
- **Username:** `root`
- **Password:** `rootpassword`
- **Schemas:** `account_db`, `user_db`, `tuition_db`

### **🚀 API SERVICES**

#### **1. AccountService (Port 8082)**
- **Base URL:** http://localhost:8082
- **Mục đích:** Quản lý tài khoản, số dư, giao dịch
- **Database:** account_db

#### **2. UserService (Port 8081)**
- **Base URL:** http://localhost:8081
- **Mục đích:** Quản lý người dùng, xác thực
- **Database:** user_db

#### **3. TuitionService (Port 8084)**
- **Base URL:** http://localhost:8084
- **Mục đích:** Quản lý học phí sinh viên
- **Database:** tuition_db

#### **4. PaymentService (Port 8083)**
- **Base URL:** http://localhost:8083
- **Mục đích:** Xử lý thanh toán, điều phối
- **Database:** Không có (sử dụng Redis)

#### **5. OTPService (Port 8085)**
- **Base URL:** http://localhost:8085
- **Mục đích:** Gửi mã OTP
- **Database:** Không có (sử dụng Redis)

## 🧪 TESTING API VỚI POSTMAN

### **📋 CÁC API ENDPOINTS CHÍNH:**

#### **AccountService (Port 8082):**
```
GET  /accounts/{userId}/balance     # Xem số dư
POST /accounts/checkBalance         # Kiểm tra số dư
GET  /accounts/{userId}/history    # Lịch sử giao dịch
POST /accounts/{userId}/lock       # Khóa tài khoản
POST /accounts/{userId}/unlock     # Mở khóa tài khoản
```

#### **UserService (Port 8081):**
```
GET  /users/{id}                   # Lấy thông tin user
POST /auth/login                   # Đăng nhập
POST /auth/register                # Đăng ký
```

#### **TuitionService (Port 8084):**
```
GET  /students/{mssv}/exists       # Kiểm tra sinh viên tồn tại
GET  /students/{mssv}              # Lấy thông tin sinh viên
POST /students/{mssv}/lock         # Khóa học phí
POST /students/{mssv}/unlock       # Mở khóa học phí
PUT  /students/{mssv}/status       # Cập nhật trạng thái học phí
```

#### **PaymentService (Port 8083):**
```
POST /payments                     # Khởi tạo thanh toán
POST /payments/confirm             # Xác nhận thanh toán
```

#### **OTPService (Port 8085):**
```
POST /otp                         # Tạo OTP
POST /otp/verify                  # Xác thực OTP
```

### **🧪 VÍ DỤ POSTMAN REQUESTS:**

#### **1. Kiểm tra số dư tài khoản:**
```http
GET http://localhost:8082/accounts/1/balance
```

#### **2. Khởi tạo thanh toán học phí:**
```http
POST http://localhost:8083/payments
Content-Type: application/json

{
  "userId": 1,
  "mssv": "SV001",
  "amount": 5000000
}
```

#### **3. Xác nhận thanh toán:**
```http
POST http://localhost:8083/payments/confirm
Content-Type: application/json

{
  "otp": "123456"
}
```

#### **4. Kiểm tra sinh viên tồn tại:**
```http
GET http://localhost:8084/students/SV001/exists
```

#### **5. Lấy thông tin sinh viên:**
```http
GET http://localhost:8084/students/SV001
```

## 🛠️ QUẢN LÝ DỰ ÁN

### **Lệnh cơ bản:**
```bash
# Khởi động dự án
docker-compose up -d

# Dừng dự án
docker-compose down

# Xem trạng thái
docker ps

# Xem logs
docker-compose logs

# Restart service cụ thể
docker-compose restart account-service
```

### **Debug và kiểm tra:**
```bash
# Vào trong container
docker exec -it ibanking-account-service bash

# Xem logs real-time
docker-compose logs -f

# Kiểm tra database (MySQL hợp nhất)
docker exec -it ibanking-mysql mysql -u root -prootpassword
```

## 🔍 TRUY CẬP DATABASE

### **Cách 1: phpMyAdmin**
- http://localhost:8080 (host `ibanking`, schemas: account_db/user_db/tuition_db)

### **Cách 2: MySQL Workbench**
- Host: `localhost`, Port: `3306`

### **Cách 3: Command Line**
```bash
docker exec -it ibanking-mysql mysql -u root -prootpassword
```

## 🚨 XỬ LÝ LỖI THƯỜNG GẶP

### **Lỗi: "Docker not running"**
```bash
# Giải pháp: Khởi động Docker Desktop trước
# Chờ icon Docker chuyển sang màu xanh
```

### **Lỗi: "Port already in use"**
```bash
# Giải pháp: Dừng containers cũ
docker-compose down
docker-compose up -d
```

### **Lỗi: "Out of memory"**
```bash
# Giải pháp: Dọn dẹp Docker
docker system prune -f
```

### **Lỗi: "Service không khởi động"**
```bash
# Xem logs chi tiết
docker-compose logs [service-name]

# Restart service
docker-compose restart [service-name]
```

## 📊 KIỂM TRA THÀNH CÔNG

### **✅ Checklist:**
- [ ] MySQL hợp nhất `ibanking:3306` (3 schema)
- [ ] phpMyAdmin 8080 hoạt động (host `ibanking`)
- [ ] Services 8081-8085 và Redis 6379 hoạt động

### **✅ Test API:**
- [ ] AccountService: http://localhost:8082/accounts/1/balance
- [ ] UserService: http://localhost:8081/users/1
- [ ] TuitionService: http://localhost:8084/students/SV001/exists
- [ ] PaymentService: http://localhost:8083/payments
- [ ] OTPService: http://localhost:8085/otp

## 💡 TIPS CHO TEAM

### **1. Luôn chạy Docker Desktop trước**
### **2. Nếu gặp lỗi, xem logs trước**
### **3. Dùng `start.bat` và `stop.bat` để dễ quản lý**
### **4. Đọc file `HUONG_DAN_SU_DUNG.md` để hiểu chi tiết**

## 🎯 TÓM TẮT CHO TEAM

**"Chỉ cần 3 bước:**
1. **Cài Docker Desktop**
2. **Clone code**
3. **Chạy `docker-compose up -d`**

**Thế là xong! Không cần cài đặt gì khác!"** 🚀

---
**Tác giả:** AI Assistant  
**Ngày tạo:** 2025-10-04  
**Phiên bản:** 1.0  
**Dành cho:** Team Development
