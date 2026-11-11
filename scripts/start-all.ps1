# ─────────────────────────────────────────────────────────────
# MediLink Tunisia - Start All Services in Windows Terminal Grid
# ─────────────────────────────────────────────────────────────

$ROOT = "C:\Users\asus\Desktop\MediLink Tunisia"

if (-not (Get-Command wt.exe -ErrorAction SilentlyContinue)) {
    Write-Host "Windows Terminal (wt.exe) est requis. Installez-le depuis le Microsoft Store." -ForegroundColor Red
    exit 1
}

# ── Service definitions ──
# Format: Name, Port, WorkingDir, Command
$services = @(
    # Row 0
    @{Name="Eureka";Port=8761;Dir="$ROOT\Backend\eureka-service";Cmd="title Eureka 8761 && mvn spring-boot:run"}
    @{Name="Config";Port=8888;Dir="$ROOT\Backend\config-server";Cmd="title Config 8888 && mvn spring-boot:run -Dspring-boot.run.profiles=native"}
    @{Name="Gateway";Port=8765;Dir="$ROOT\Backend\api-gateway";Cmd="title Gateway 8765 && mvn spring-boot:run"}
    # Row 1
    @{Name="Auth";Port=8084;Dir="$ROOT\Backend\auth-service";Cmd="title Auth 8084 && mvn spring-boot:run"}
    @{Name="Patient";Port=8082;Dir="$ROOT\Backend\patient-service";Cmd="title Patient 8082 && mvn spring-boot:run"}
    @{Name="Doctor";Port=8083;Dir="$ROOT\Backend\doctor-service";Cmd="title Doctor 8083 && mvn spring-boot:run"}
    # Row 2
    @{Name="Pharmacy";Port=8085;Dir="$ROOT\Backend\pharmacy-service";Cmd="title Pharmacy 8085 && mvn spring-boot:run"}
    @{Name="Prescrip";Port=8086;Dir="$ROOT\Backend\prescription-service";Cmd="title Prescrip 8086 && mvn spring-boot:run"}
    @{Name="Bilan";Port=8087;Dir="$ROOT\Backend\bilan-service";Cmd="title Bilan 8087 && mvn spring-boot:run"}
    # Row 3
    @{Name="Monitor";Port=8090;Dir="$ROOT\Backend\monitoring-service";Cmd="title Monitor 8090 && mvn spring-boot:run"}
    @{Name="AI";Port=8093;Dir="$ROOT\Backend\ai-service";Cmd="title AI 8093 && call venv\Scripts\activate.bat && uvicorn app.main:app --host 0.0.0.0 --port 8093 --reload"}
    @{Name="Angular";Port=4200;Dir="$ROOT\Frontend\medilink-angular";Cmd="title Angular 4200 && ng serve"}
    # Row 4
    @{Name="Mobile";Port=8081;Dir="$ROOT\Mobile App";Cmd="title Mobile Expo && npx expo start"}
    @{Name="n8n";Port=5678;Dir="$ROOT";Cmd="title n8n 5678 && call n8n-start.bat"}
)

$cols = 3
$wtArgs = New-Object System.Collections.ArrayList

# ── Build Windows Terminal command ──
# Strategy: create 3 columns in first row, then walk left and split-H for each subsequent row

$wtArgs.AddRange(@(
    "--window", "0", "new-tab", "--title", "MediLink Tunisia",
    "-d", $services[0].Dir, "cmd", "/c", $services[0].Cmd
))

# First row: vertical splits for cols 2 and 3
$wtArgs.Add(";")
$wtArgs.AddRange(@("split-pane", "-V", "-d", $services[1].Dir, "cmd", "/c", $services[1].Cmd))

$wtArgs.Add(";")
$wtArgs.AddRange(@("split-pane", "-V", "-d", $services[2].Dir, "cmd", "/c", $services[2].Cmd))

# For rows 2-5: move to first column, then horizontal split for each column
for ($row = 1; $row -lt 5; $row++) {
    # Move focus left to first column (cols - 1 times)
    for ($m = 0; $m -lt $cols - 1; $m++) {
        $wtArgs.Add(";")
        $wtArgs.Add("move-focus")
        $wtArgs.Add("left")
    }

    # For each column in this row
    for ($c = 0; $c -lt $cols; $c++) {
        $idx = $row * $cols + $c
        if ($idx -ge $services.Count) { break }
        $svc = $services[$idx]

        $wtArgs.Add(";")
        $wtArgs.AddRange(@("split-pane", "-H", "-d", $svc.Dir, "cmd", "/c", $svc.Cmd))

        # Move right for next column (if not the last)
        if ($c -lt $cols - 1 -and ($idx + 1) -lt $services.Count) {
            $wtArgs.Add(";")
            $wtArgs.Add("move-focus")
            $wtArgs.Add("right")
        }
    }
}

# ── Launch Windows Terminal ──
Write-Host "══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  MediLink Tunisia - Demarrage de tous les services" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ouverture de Windows Terminal avec la grille de services..." -ForegroundColor Yellow

try {
    Start-Process -FilePath "wt.exe" -ArgumentList $wtArgs -WindowStyle Normal
    Write-Host "✅ Terminal lance avec tous les services en grille" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Services dans le terminal unique :" -ForegroundColor Gray
Write-Host "  Ligne 1 : Eureka (8761) | Config (8888) | Gateway (8765)" -ForegroundColor Gray
Write-Host "  Ligne 2 : Auth (8084) | Patient (8082) | Doctor (8083)" -ForegroundColor Gray
Write-Host "  Ligne 3 : Pharmacy (8085) | Prescription (8086) | Bilan (8087)" -ForegroundColor Gray
Write-Host "  Ligne 4 : Monitoring (8090) | AI Service (8093) | Angular (4200)" -ForegroundColor Gray
Write-Host "  Ligne 5 : Mobile (Expo) | n8n (5678)" -ForegroundColor Gray
Write-Host ""
Write-Host "Utilisez stop-all.bat pour arreter" -ForegroundColor Yellow
