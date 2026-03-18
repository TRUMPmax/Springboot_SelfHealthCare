@echo off
setlocal
chcp 65001 >nul

cd /d "%~dp0"

set "APP_URL=http://localhost:8081"
set "H2_URL=http://localhost:8081/h2-console"

echo ==========================================
echo   Self Health Care - Quick Start
echo ==========================================
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java was not found.
    echo [HINT] Please install JDK 21 or newer, then run: java -version
    echo.
    pause
    exit /b 1
)

if not exist "mvnw.cmd" (
    echo [ERROR] mvnw.cmd was not found in the current folder.
    echo [HINT] Please keep this script in the project root folder.
    echo.
    pause
    exit /b 1
)

echo [INFO] Starting Spring Boot application in a new window...
echo [INFO] Home page : %APP_URL%
echo [INFO] H2 console: %H2_URL%
echo.
echo [HINT] The first startup may take longer while Maven downloads dependencies.
echo [HINT] The browser will open automatically after the app is ready.
echo.

start "Self Health Care Server" cmd /k "cd /d ""%~dp0"" && .\mvnw.cmd spring-boot:run"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$deadline=(Get-Date).AddSeconds(90);" ^
    "while((Get-Date) -lt $deadline){" ^
    "  try {" ^
    "    Invoke-WebRequest -Uri '%APP_URL%' -UseBasicParsing -TimeoutSec 3 | Out-Null;" ^
    "    Start-Process '%APP_URL%';" ^
    "    exit 0" ^
    "  } catch {" ^
    "    Start-Sleep -Seconds 2" ^
    "  }" ^
    "}" ^
    "exit 1"

if errorlevel 1 (
    echo [WARN] The app did not become ready within 90 seconds.
    echo [HINT] You can open the page manually later: %APP_URL%
    echo.
    pause
    exit /b 1
)

echo [INFO] Browser opened: %APP_URL%
echo.
pause
