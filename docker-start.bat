@echo off
REM =============================================================================
REM MediLink Tunisia — Docker Start All
REM Starts all services defined in docker-compose.yml
REM =============================================================================
echo Starting MediLink Tunisia...
echo.

REM Verify .env exists
if not exist .env (
    echo WARNING: .env file not found. Copying from .env.example...
    copy .env.example .env
    echo Please edit .env to add your GEMINI_API_KEY before starting.
    echo.
)

docker compose up -d

echo.
echo Services starting... Check status with: docker compose ps
echo.
echo Key URLs:
echo   Frontend:    http://localhost
echo   Mobile App:  http://localhost:8080
echo   API Gateway: http://localhost:8765
echo   Eureka:      http://localhost:8761
echo   n8n:         http://localhost:5678
echo   Grafana:     http://localhost:3000 (admin/medilink2025)
echo   Prometheus:  http://localhost:9090
