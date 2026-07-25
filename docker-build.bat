@echo off
REM =============================================================================
REM MediLink Tunisia — Docker Build All
REM Builds all Docker images for the MediLink Tunisia microservices
REM =============================================================================
echo Building MediLink Tunisia Docker images...
echo.

echo [1/3] Building Spring Boot services (parallel)...
docker compose build ^
    eureka-service config-server api-gateway ^
    auth-service patient-service doctor-service ^
    pharmacy-service prescription-service bilan-service ^
    monitoring-service

echo.
echo [2/3] Building AI Service...
docker compose build ai-service

echo.
echo [3/3] Building Frontend...
docker compose build frontend

echo.
echo [4/4] Building Mobile App (web export)...
docker compose build mobile

echo.
echo All builds complete!
echo Run "docker compose up -d" to start all services.
