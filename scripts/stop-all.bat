@echo off
title MediLink Stopper

echo ^═══════════════════════════════════════════════════════
echo   MEDILINK TUNISIA - STOP ALL SERVICES
echo ^═══════════════════════════════════════════════════════
echo.

:: Tuer tous les processus Java (microservices Spring Boot)
echo  [1] Arret des microservices Java...
taskkill /F /IM java.exe /T 2>nul
if %errorlevel% equ 0 ( echo   [OK] Java (Spring Boot) arretes
) else ( echo   [-] Aucun processus Java trouve )

:: Tuer les processus Python/Uvicorn (AI Service)
echo.
echo  [2] Arret de l'AI Service Python...
taskkill /F /IM python.exe /T 2>nul 1>nul
taskkill /F /IM uvicorn.exe /T 2>nul 1>nul
echo   [OK] Processus Python/Uvicorn arretes

:: Tuer les processus Node (Angular, Expo)
echo.
echo  [3] Arret des processus Node (Angular, Expo)...
:: On tue node.exe mais pas n8n.cmd
taskkill /F /IM node.exe /T 2>nul 1>nul
echo   [OK] Node (Angular/Expo) arretes

:: Tuer n8n specifiquement
echo.
echo  [4] Arret de n8n...
taskkill /F /IM n8n.exe /T 2>nul 1>nul
taskkill /F /IM n8n.cmd /T 2>nul 1>nul
echo   [OK] n8n arretes

:: Tuer cloudflared tunnel
echo.
echo  [5] Arret du tunnel cloudflared...
taskkill /F /IM cloudflared.exe /T 2>nul 1>nul
echo   [OK] cloudflared arretes

:: Fermer Windows Terminal
echo.
echo  [6] Fermeture de Windows Terminal...
taskkill /F /IM wt.exe 2>nul 1>nul
echo   [OK] Terminal ferme

echo.
echo ^═══════════════════════════════════════════════════════
echo   TOUS LES SERVICES SONT ARRETES
echo ^═══════════════════════════════════════════════════════
echo.
pause
