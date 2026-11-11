# Lance le tunnel cloudflared vers n8n et enregistre automatiquement
# la nouvelle URL publique comme webhook du bot Telegram.
# Prérequis : n8n doit déjà tourner sur http://localhost:5678 (n8n-start.bat).
# Laisser cette fenêtre ouverte : la fermer coupe le tunnel (les boutons Telegram ne marchent plus).

$ErrorActionPreference = "Stop"
$Cloudflared = "C:\Program Files (x86)\cloudflared\cloudflared.exe"
$BotToken    = "8644047281:AAHywXMMM9zpDThyBj_FOgme46RZi_GhzRk"
$WebhookPath = "/webhook/tZwHDkw1PqSAYw1F/webhook/telegram-callback"
$LogFile     = Join-Path $env:TEMP "cloudflared-medilink.log"

Write-Host "== MediLink : tunnel Telegram -> n8n ==" -ForegroundColor Cyan

# 1. Attendre que n8n réponde (lancé en parallèle par "Start All" : jusqu'à 3 min)
$n8nUp = $false
for ($i = 0; $i -lt 90 -and -not $n8nUp; $i++) {
    try {
        Invoke-WebRequest -Uri "http://localhost:5678/healthz" -UseBasicParsing -TimeoutSec 3 | Out-Null
        $n8nUp = $true
    } catch {
        if ($i -eq 0) { Write-Host "En attente de n8n sur http://localhost:5678..." }
        Start-Sleep -Seconds 2
    }
}
if (-not $n8nUp) {
    Write-Host "[ERREUR] n8n ne repond pas sur http://localhost:5678 apres 3 min. Lance d'abord n8n-start.bat." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] n8n repond sur le port 5678" -ForegroundColor Green

# 2. Un seul tunnel à la fois
Get-Process cloudflared -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
if (Test-Path $LogFile) { Remove-Item $LogFile -Force }

# 3. Démarrage du tunnel
$proc = Start-Process -FilePath $Cloudflared `
    -ArgumentList "tunnel --no-autoupdate --url http://localhost:5678 --logfile `"$LogFile`"" `
    -PassThru -WindowStyle Hidden
Write-Host "Demarrage du tunnel cloudflared (PID $($proc.Id))..."

$url = $null
for ($i = 0; $i -lt 60 -and -not $url; $i++) {
    Start-Sleep -Seconds 1
    if (Test-Path $LogFile) {
        $m = Select-String -Path $LogFile -Pattern "https://[a-z0-9-]+\.trycloudflare\.com" | Select-Object -First 1
        if ($m) { $url = $m.Matches[0].Value }
    }
    if ($proc.HasExited) { break }
}
if (-not $url) {
    Write-Host "[ERREUR] Impossible d'obtenir l'URL du tunnel. Journal : $LogFile" -ForegroundColor Red

    exit 1
}
Write-Host "[OK] Tunnel : $url" -ForegroundColor Green

# 4. Attendre que l'URL publique soit joignable (propagation DNS)
$reachable = $false
for ($i = 0; $i -lt 30 -and -not $reachable; $i++) {
    try {
        Invoke-WebRequest -Uri "$url/healthz" -UseBasicParsing -TimeoutSec 5 | Out-Null
        $reachable = $true
    } catch { Start-Sleep -Seconds 2 }
}
if ($reachable) { Write-Host "[OK] n8n joignable via le tunnel" -ForegroundColor Green }
else { Write-Host "[ATTENTION] URL pas encore joignable, on enregistre quand meme le webhook." -ForegroundColor Yellow }

# 5. Enregistrement du webhook Telegram (les anciens clics en attente sont ignorés)
$hook = [uri]::EscapeDataString("$url$WebhookPath")
$res = Invoke-RestMethod -Uri "https://api.telegram.org/bot$BotToken/setWebhook?drop_pending_updates=true&url=$hook"
if ($res.ok) { Write-Host "[OK] Webhook Telegram enregistre : $url$WebhookPath" -ForegroundColor Green }
else { Write-Host "[ERREUR] setWebhook : $($res.description)" -ForegroundColor Red }

Write-Host ""
Write-Host "Tout est pret. Laisse cette fenetre ouverte (Ctrl+C pour arreter le tunnel)." -ForegroundColor Cyan
try {
    Wait-Process -Id $proc.Id
} finally {
    if (-not $proc.HasExited) { Stop-Process -Id $proc.Id -Force }
    Write-Host "Tunnel arrete : les boutons Telegram ne fonctionneront plus jusqu'au prochain lancement." -ForegroundColor Yellow
}
