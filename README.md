# 🏦 iBanking System - Docker Setup

Hệ thống iBanking với microservices architecture sử dụng Docker.

## 📋 Yêu cầu hệ thống

- Docker Desktop
- Windows 10/11
- 8GB RAM trở lên
- 10GB dung lượng trống

## 🚀 Cách sử dụng

### 1. Khởi động hệ thống
```bash
# Chạy script khởi động
start.bat

# Hoặc chạy thủ công
docker-compose up -d
```

### 2. Kiểm tra trạng thái
```bash
# Xem tất cả containers
docker-compose ps

# Xem logs của service
docker-compose logs -f account-service
```

### 3. Dừng hệ thống
```bash
# Chạy script dừng
stop.bat

# Hoặc chạy thủ công
docker-compose down
```

## 🌐 Services

| Service | Port | URL | Mô tả |
|---------|------|-----|-------|
| Account Service | 8082 | http://localhost:8082 | Quản lý tài khoản |
| Payment Service | 8083 | http://localhost:8083 | Xử lý thanh toán |
| Tuition Service | 8084 | http://localhost:8084 | Quản lý học phí |
| OTP Service | 8085 | http://localhost:8085 | Gửi OTP |
| MySQL | 3306 | localhost:3306 | Database |
| Redis | 6379 | localhost:6379 | Cache & Locking |

## 🧪 Test APIs

### Test Account Service
```bash
# Lấy số dư
curl -X GET "http://localhost:8082/accounts/123456789/balance"

# Lấy lịch sử
curl -X GET "http://localhost:8082/accounts/123456789/history"
```

### Test Payment Service
```bash
# Khởi tạo thanh toán
curl -X POST "http://localhost:8083/payments/initiate" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123456789,
    "mssv": "20123456",
    "amount": 100.50
  }'
```

## 🔧 Troubleshooting

### Lỗi thường gặp

1. **Port đã được sử dụng**
   ```bash
   # Kiểm tra port
   netstat -an | findstr :8082
   
   # Dừng process
   taskkill /PID <PID> /F
   ```

2. **Container không khởi động**
   ```bash
   # Xem logs
   docker-compose logs account-service
   
   # Restart service
   docker-compose restart account-service
   ```

3. **Database connection failed**
   ```bash
   # Kiểm tra MySQL
   docker-compose logs mysql
   
   # Restart MySQL
   docker-compose restart mysql
   ```

### Commands hữu ích

```bash
# Xem tất cả containers
docker ps -a

# Xem images
docker images

# Xóa tất cả containers
docker-compose down --remove-orphans

# Xóa tất cả images
docker system prune -a

# Xem resource usage
docker stats
```

## 📁 Cấu trúc thư mục

```
ibanking/
├── AccountService/
│   ├── Dockerfile
│   └── src/
├── PaymentService/
│   ├── Dockerfile
│   └── src/
├── TuitionService/
│   ├── Dockerfile
│   └── src/
├── OTPNotificationService/
│   ├── Dockerfile
│   └── src/
├── database/
│   └── account.sql
├── docker-compose.yml
├── start.bat
├── stop.bat
└── README.md
```

## 🆘 Hỗ trợ

Nếu gặp vấn đề, hãy kiểm tra:
1. Docker Desktop đang chạy
2. Ports không bị conflict
3. Đủ RAM và dung lượng
4. Logs của từng service

---
**Happy Coding! 🎉**
