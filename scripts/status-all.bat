@echo off
setlocal enabledelayedexpansion
title MediLink Status

cd /d "%~dp0.."

echo ^═══════════════════════════════════════════════════════
echo   MEDILINK TUNISIA - SERVICE STATUS CHECK
echo ^═══════════════════════════════════════════════════════
echo.
echo  Port    Service            Status
echo  ──────────────────────────────────────

set SERVICES=8761:Eureka 8888:Config 8765:Gateway 8084:Auth 8082:Patient 8083:Doctor 8085:Pharmacy 8086:Prescription 8087:Bilan 8090:Monitoring 8093:AI 4200:Angular 5678:n8n

for %%s in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        netstat -ano 2>nul | findstr /C:":%%a " >nul 2>&1
        if !errorlevel! equ 0 (
            echo  [%%a]  %%~b                  [OK]
        ) else (
            echo  [%%a]  %%~b                  [FAIL]
        )
    )
)

echo.
echo  Use scripts\start-all.bat to start all services.
echo.
pause
