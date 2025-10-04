# 📚 HƯỚNG DẪN SỬ DỤNG DỰ ÁN IBANKING

## 🎯 TỔNG QUAN DỰ ÁN

Dự án iBanking sử dụng kiến trúc **Microservices** với **Docker** và **Database riêng biệt** cho từng service.

### 📊 Kiến trúc hệ thống:
```
┌─────────────────────────────────────────────────────────┐
│                MICROSERVICES + DATABASES               │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │Account DB   │  │User DB     │  │Tuition DB   │     │
│  │Port 3306    │  │Port 3307   │  │Port 3308   │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
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

## 🚀 QUY TRÌNH KHỞI ĐỘNG DỰ ÁN

### **Bước 1: Chuẩn bị môi trường**
1. **Đảm bảo Docker Desktop đang chạy** (icon Docker màu xanh)
2. **Mở terminal** (PowerShell hoặc Command Prompt)
3. **Di chuyển đến thư mục dự án:**
   ```bash
   cd D:\test\ibanking
   ```

### **Bước 2: Khởi động tất cả services**
```bash
# Khởi động tất cả services
docker-compose up -d

# Kiểm tra trạng thái
docker ps
```

### **Bước 3: Kiểm tra logs (nếu cần)**
```bash
# Xem logs của tất cả services
docker-compose logs

# Xem logs của service cụ thể
docker-compose logs account-service
docker-compose logs user-service
docker-compose logs tuition-service
```

## 📊 TRUY CẬP CÁC DỊCH VỤ

### **🌐 Web Interfaces:**
- **phpMyAdmin:** http://localhost:8080
  - Username: `account_user`
  - Password: `account123`
  - Database: `account_db`

### **🗄️ Database Connections:**
| **Service** | **Database** | **Port** | **Username** | **Password** |
|-------------|--------------|----------|--------------|--------------|
| AccountService | account_db | 3306 | account_user | account123 |
| UserService | user_db | 3307 | user_user | user123 |
| TuitionService | tuition_db | 3308 | tuition_user | tuition123 |

### **🔧 API Endpoints:**
| **Service** | **Port** | **Base URL** |
|-------------|----------|--------------|
| AccountService | 8082 | http://localhost:8082 |
| UserService | 8081 | http://localhost:8081 |
| TuitionService | 8084 | http://localhost:8084 |
| PaymentService | 8083 | http://localhost:8083 |
| OTPService | 8085 | http://localhost:8085 |

## 🛠️ QUẢN LÝ DỰ ÁN

### **Dừng tất cả services:**
```bash
docker-compose down
```

### **Khởi động lại services:**
```bash
docker-compose up -d
```

### **Xem trạng thái containers:**
```bash
docker ps
```

### **Xem logs của service cụ thể:**
```bash
docker-compose logs [service-name]
```

### **Vào trong container để debug:**
```bash
docker exec -it ibanking-account-service bash
docker exec -it ibanking-user-service bash
docker exec -it ibanking-tuition-service bash
```

## 🔍 KIỂM TRA VÀ DEBUG

### **Kiểm tra kết nối database:**
```bash
# Account Database
docker-compose exec account-db mysql -u account_user -p account_db

# User Database
docker-compose exec user-db mysql -u user_user -p user_db

# Tuition Database
docker-compose exec tuition-db mysql -u tuition_user -p tuition_db
```

### **Kiểm tra Redis:**
```bash
docker-compose exec redis redis-cli
```

### **Kiểm tra API endpoints:**
```bash
# Test AccountService
curl http://localhost:8082/accounts/1/balance

# Test UserService
curl http://localhost:8081/users/1

# Test TuitionService
curl http://localhost:8084/students/SV001/exists
```

## 🚨 XỬ LÝ LỖI THƯỜNG GẶP

### **Lỗi: Container không khởi động**
```bash
# Xem logs chi tiết
docker-compose logs [service-name]

# Restart service cụ thể
docker-compose restart [service-name]

# Rebuild service
docker-compose build [service-name]
docker-compose up -d [service-name]
```

### **Lỗi: Database connection failed**
```bash
# Kiểm tra database container
docker ps | grep db

# Restart database
docker-compose restart account-db
docker-compose restart user-db
docker-compose restart tuition-db
```

### **Lỗi: Port đã được sử dụng**
```bash
# Kiểm tra port đang sử dụng
netstat -ano | findstr :8082
netstat -ano | findstr :3306

# Dừng tất cả containers
docker-compose down

# Khởi động lại
docker-compose up -d
```

### **Lỗi: Out of memory**
```bash
# Dọn dẹp Docker
docker system prune -f

# Kiểm tra dung lượng
docker system df
```

## 🔄 QUY TRÌNH TẮT MÁY VÀ MỞ LẠI

### **📴 TẮT MÁY AN TOÀN:**
1. **Dừng containers trước:**
   ```bash
   cd D:\test\ibanking
   docker-compose down
   ```
2. **Tắt Docker Desktop** (tùy chọn)
3. **Tắt máy bình thường**

### **🚀 MỞ LẠI MÁY:**
1. **Khởi động Docker Desktop** (chờ icon xanh)
2. **Mở terminal:**
   ```bash
   cd D:\test\ibanking
   ```
3. **Khởi động services:**
   ```bash
   docker-compose up -d
   ```
4. **Kiểm tra trạng thái:**
   ```bash
   docker ps
   ```

## 📝 LỆNH HỮU ÍCH

### **Quản lý Docker:**
```bash
# Xem tất cả containers
docker ps -a

# Xem images
docker images

# Xem volumes
docker volume ls

# Dọn dẹp không sử dụng
docker system prune -f

# Xem dung lượng sử dụng
docker system df
```

### **Quản lý dự án:**
```bash
# Build lại tất cả
docker-compose build

# Build service cụ thể
docker-compose build account-service

# Xem logs real-time
docker-compose logs -f

# Restart tất cả
docker-compose restart
```

## 🎯 TESTING API VỚI POSTMAN

### **Các API endpoints chính:**

#### **AccountService (Port 8082):**
```
GET  /accounts/{userId}/balance     # Xem số dư
POST /accounts/checkBalance         # Kiểm tra số dư
GET  /accounts/{userId}/history    # Lịch sử giao dịch
```

#### **UserService (Port 8081):**
```
GET  /users/{id}                   # Lấy thông tin user
```

#### **TuitionService (Port 8084):**
```
GET  /students/{mssv}/exists       # Kiểm tra sinh viên tồn tại
GET  /students/{mssv}              # Lấy thông tin sinh viên
```

#### **PaymentService (Port 8083):**
```
POST /payments                     # Khởi tạo thanh toán
POST /payments/confirm             # Xác nhận thanh toán
```

### **Ví dụ Postman requests:**

#### **1. Kiểm tra số dư:**
```
GET http://localhost:8082/accounts/1/balance
```

#### **2. Khởi tạo thanh toán:**
```
POST http://localhost:8083/payments
Content-Type: application/json

{
  "userId": 1,
  "mssv": "SV001",
  "amount": 5000000
}
```

#### **3. Kiểm tra sinh viên:**
```
GET http://localhost:8084/students/SV001/exists
```

## 📞 HỖ TRỢ

Nếu gặp vấn đề, hãy kiểm tra:
1. **Docker Desktop đang chạy**
2. **Ports không bị xung đột**
3. **Đủ dung lượng ổ cứng**
4. **Logs của services** để xem lỗi cụ thể

---
**Tác giả:** AI Assistant  
**Ngày tạo:** 2025-10-04  
**Phiên bản:** 1.0
