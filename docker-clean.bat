@echo off
REM =============================================================================
REM MediLink Tunisia — Docker Clean
REM Stops all services and removes volumes (data will be lost!)
REM =============================================================================
echo WARNING: This will DELETE ALL DATA volumes!
echo Press Ctrl+C within 5 seconds to cancel...
timeout /t 5 /nobreak >nul

echo Stopping and removing containers...
docker compose down -v

echo.
echo Done. Run "docker-start" to rebuild from scratch.
