@echo off
REM =============================================================================
REM MediLink Tunisia — Follow Logs
REM Usage: docker-logs          (all services)
REM        docker-logs <name>   (specific service, e.g. docker-logs auth-service)
REM =============================================================================
if "%1"=="" (
    docker compose logs -f
) else (
    docker compose logs -f %1
)
