@echo off
title MediLink Launcher
cd /d "C:\Users\asus\Desktop\MediLink Tunisia"

echo ========================================
echo   MEDILINK TUNISIA - START ALL SERVICES
echo ========================================
echo.
echo Ouverture de chaque service dans une fenetre separee...
echo.

:: ── Row 1: Infrastructure ──
start "Eureka 8761" cmd /c "title Eureka 8761 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\eureka-service" && mvn spring-boot:run"
start "Config 8888" cmd /c "title Config 8888 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\config-server" && mvn spring-boot:run -Dspring-boot.run.profiles=native"
start "Gateway 8765" cmd /c "title Gateway 8765 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\api-gateway" && mvn spring-boot:run"

:: ── Row 2: Core services ──
start "Auth 8084" cmd /c "title Auth 8084 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\auth-service" && mvn spring-boot:run"
start "Patient 8082" cmd /c "title Patient 8082 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\patient-service" && mvn spring-boot:run"
start "Doctor 8083" cmd /c "title Doctor 8083 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\doctor-service" && mvn spring-boot:run"

:: ── Row 3: Business services ──
start "Pharmacy 8085" cmd /c "title Pharmacy 8085 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\pharmacy-service" && mvn spring-boot:run"
start "Prescrip 8086" cmd /c "title Prescrip 8086 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\prescription-service" && mvn spring-boot:run"
start "Bilan 8087" cmd /c "title Bilan 8087 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\bilan-service" && mvn spring-boot:run"

:: ── Row 4: Monitoring, AI, Frontend ──
start "Monitor 8090" cmd /c "title Monitor 8090 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\monitoring-service" && mvn spring-boot:run"
start "AI 8093" cmd /c "title AI 8093 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Backend\ai-service" && call venv\Scripts\activate.bat && uvicorn app.main:app --host 0.0.0.0 --port 8093 --reload"
start "Angular 4200" cmd /c "title Angular 4200 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Frontend\medilink-angular" && ng serve"

:: ── Row 5: Mobile, n8n ──
start "Mobile Expo" cmd /c "title Mobile Expo && cd /d "C:\Users\asus\Desktop\MediLink Tunisia\Mobile App" && npx expo start"
start "n8n 5678" cmd /c "title n8n 5678 && cd /d "C:\Users\asus\Desktop\MediLink Tunisia" && call n8n-start.bat"

echo.
echo  %date% %time% - Tous les services ont ete lances.
echo  Chaque service est dans sa propre fenetre.
echo  Utilisez stop-all.bat pour tout arreter.
echo.
pause
