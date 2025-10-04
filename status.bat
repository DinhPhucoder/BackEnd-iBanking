@echo off
echo ========================================
echo    TRANG THAI DU AN IBANKING
echo ========================================
echo.

echo [1/4] Kiem tra Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker chua duoc cai dat hoac chua chay!
    pause
    exit /b 1
)
echo ✅ Docker da san sang

echo.
echo [2/4] Di chuyen den thu muc du an...
cd /d "D:\test\ibanking"
echo ✅ Da den thu muc: %CD%

echo.
echo [3/4] Trang thai containers...
echo.
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo [4/4] Thong tin chi tiet...
echo.
echo 📊 SERVICES:
for /f "tokens=*" %%i in ('docker ps --format "{{.Names}}"') do (
    echo    - %%i
)

echo.
echo 🌐 PORTS:
echo    - phpMyAdmin: http://localhost:8080
echo    - AccountService: http://localhost:8082
echo    - UserService: http://localhost:8081
echo    - TuitionService: http://localhost:8084
echo    - PaymentService: http://localhost:8083
echo    - OTPService: http://localhost:8085

echo.
echo 📊 DATABASES:
echo    - Account DB: localhost:3306
echo    - User DB: localhost:3307
echo    - Tuition DB: localhost:3308

echo.
echo ========================================
echo Nhan phim bat ky de dong cua so...
pause >nul
