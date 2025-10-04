@echo off
echo ========================================
echo    DUNG DU AN IBANKING
echo ========================================
echo.

echo [1/3] Di chuyen den thu muc du an...
cd /d "D:\test\ibanking"
echo ✅ Da den thu muc: %CD%

echo.
echo [2/3] Dung tat ca services...
docker-compose down
if %errorlevel% neq 0 (
    echo ❌ Co loi khi dung services!
    pause
    exit /b 1
)

echo.
echo [3/3] Kiem tra trang thai...
docker ps

echo.
echo ========================================
echo    DA DUNG TAT CA SERVICES!
echo ========================================
echo.
echo 💡 Luu y: Du lieu van duoc luu trong Docker volumes
echo    De xoa hoan toan, su dung: docker-compose down -v
echo.
echo Nhan phim bat ky de dong cua so...
pause >nul