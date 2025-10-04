@echo off
echo ========================================
echo    KHOI DONG DU AN IBANKING
echo ========================================
echo.

echo [1/4] Kiem tra Docker Desktop...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker chua duoc cai dat hoac chua chay!
    echo Vui long khoi dong Docker Desktop truoc.
    pause
    exit /b 1
)
echo ✅ Docker da san sang

echo.
echo [2/4] Di chuyen den thu muc du an...
cd /d "D:\test\ibanking"
echo ✅ Da den thu muc: %CD%

echo.
echo [3/4] Khoi dong tat ca services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ Co loi khi khoi dong services!
    pause
    exit /b 1
)

echo.
echo [4/4] Kiem tra trang thai...
timeout /t 5 /nobreak >nul
docker ps

echo.
echo ========================================
echo    KHOI DONG THANH CONG!
echo ========================================
echo.
echo 🌐 Truy cap cac dich vu:
echo    - phpMyAdmin: http://localhost:8080
echo    - AccountService: http://localhost:8082
echo    - UserService: http://localhost:8081
echo    - TuitionService: http://localhost:8084
echo    - PaymentService: http://localhost:8083
echo    - OTPService: http://localhost:8085
echo.
echo 📊 Databases:
echo    - Account DB: localhost:3306
echo    - User DB: localhost:3307
echo    - Tuition DB: localhost:3308
echo.
echo Nhan phim bat ky de dong cua so...
pause >nul