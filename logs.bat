@echo off
echo ========================================
echo    XEM LOGS DU AN IBANKING
echo ========================================
echo.

echo [1/3] Di chuyen den thu muc du an...
cd /d "D:\test\ibanking"
echo ✅ Da den thu muc: %CD%

echo.
echo [2/3] Chon service de xem logs:
echo    1. Tat ca services
echo    2. AccountService
echo    3. UserService
echo    4. TuitionService
echo    5. PaymentService
echo    6. OTPService
echo    7. Databases
echo.

set /p choice="Nhap lua chon (1-7): "

if "%choice%"=="1" (
    echo.
    echo 📋 Logs cua tat ca services:
    docker-compose logs
) else if "%choice%"=="2" (
    echo.
    echo 📋 Logs cua AccountService:
    docker-compose logs account-service
) else if "%choice%"=="3" (
    echo.
    echo 📋 Logs cua UserService:
    docker-compose logs user-service
) else if "%choice%"=="4" (
    echo.
    echo 📋 Logs cua TuitionService:
    docker-compose logs tuition-service
) else if "%choice%"=="5" (
    echo.
    echo 📋 Logs cua PaymentService:
    docker-compose logs payment-service
) else if "%choice%"=="6" (
    echo.
    echo 📋 Logs cua OTPService:
    docker-compose logs otp-service
) else if "%choice%"=="7" (
    echo.
    echo 📋 Logs cua Databases:
    docker-compose logs account-db
    docker-compose logs user-db
    docker-compose logs tuition-db
) else (
    echo ❌ Lua chon khong hop le!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Nhan phim bat ky de dong cua so...
pause >nul
