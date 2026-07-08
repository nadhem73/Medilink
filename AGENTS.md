# MediLink Tunisia – Session Context

## Goal
Finaliser le flux notifications Telegram patient via n8n. Ajouter la recherche/filtres sur la page ordonnances pharmacie.

## Constraints & Preferences
- **Gratuit** : Telegram Bot API uniquement.
- **n8n 2.8.4** installé via npm, tourne sur `http://localhost:5678`.
- **Bot Telegram** : `@medilink_tunisie_bot` (token `8644047281:AAHywXMMM9zpDThyBj_FOgme46RZi_GhzRk`).

## Progress
### ✅ Done
- **Recherche + Filtres (Pharmacy) — redesign** : Barre de recherche avec icône SVG, bouton tri avec libellé (Récent/Ancien), filtres par statut en pillules cliquables (remplace le `<select>`), état vide filtré avec bouton "Effacer les filtres". Layout en 3 zones distinctes (header/search+pills/liste). Build OK.

### ❌ Expression evaluation (non essentiel)
- Les expressions n8n (`=$json.body.field` ou `{{ $json.field }}`) ne sont PAS évaluées dans n8n 2.8.4. Contourné : Code node + `this.helpers.httpRequest()`.

## Current Architecture

### Workflow 1: `prescription-created` (ID: `iYlm5Sv4hR97rixn`)
Webhook POST → Code node (appelle Telegram API directement) → Respond to Webhook
- **Webhook path** : `prescription-created`
- **Code node** : lit `$input.first().json.body.patientTelegramChatId`, appelle `https://api.telegram.org/bot<TOKEN>/sendMessage`

### Workflow 2: `telegram-callback` (ID: `tZwHDkw1PqSAYw1F`)
Telegram Trigger (callback_query) → Code node → Assignation pharmacie
- **Trigger** : écoute les callbacks Telegram (boutons inline)
- **Code node** : parse `callback_data` (format `pick:{prescriptionId}:{pharmacyId}`), fetch pharmacy name, assigne pharmacie via backend, répond au callback, envoie confirmation au patient

### Workflow 3: `prescription-prepared` (ID: `ScrkfsBPpbxh1227`)
Webhook POST → Code node (validation + Telegram API) → Respond to Webhook
- **Webhook path** : `prescription-prepared`
- **Code node** : valide `patientTelegramChatId` et `pickupCode`, envoie message Telegram avec code de retrait

### Backend (`notifyN8n()` in `PrescriptionService.java`)
- Lance `POST /webhook/{WF_ID}/webhook/{path}` pour chaque événement
- Mappe : `prescription-created` → WF1, `prescription-prepared` → WF3
- Envoie `{ id, patientId, doctorId, status, patientTelegramChatId, baseUrl }`

## Next Steps
1. **Reconstruire et tester le backend** avec les webhooks n8n.
2. **Persistance n8n** : n8n est lancé manuellement via `.bat`. Prévoir un service Windows ou PM2 pour la production.
3. **Nettoyer** : supprimer WF `TEST-Expression Evaluation` et les exécutions d'erreur.

## Critical Context
- **n8n 2.8.4** : Les expressions n8n (`=$json` / `{{ }}`) ne fonctionnent pas. Solution : Code node + `this.helpers.httpRequest()`.
- **Installation** : nvm4w (`C:\nvm4w\nodejs\node_modules\n8n`), Node.js v20.19.3.
- **Process** : lancé via `n8n-start.bat` avec `N8N_WEBHOOK_URL` pointant vers cloudflared. PID variable, port 5678.
- **Tunnel** : cloudflared.exe (process séparé) fournit HTTPS. Vérifier l'URL cloudflared via la métrique `cloudflared_tunnel_user_hostnames_counts`.
- **DB n8n** : `~/.n8n/database.sqlite` (nouvelle base depuis la réinstallation).
- **ChatId Telegram** : `6435558113` (Nadhem, `@Nadhem_hm`).
- **Token Telegram** : `8644047281:AAHywXMMM9zpDThyBj_FOgme46RZi_GhzRk`.

## Relevant Files
- **`C:\nvm4w\nodejs\node_modules\n8n`** : installation n8n actuelle
- **`C:\nvm4w\nodejs\n8n.cmd`** : wrapper de commande n8n
- **`C:\Users\asus\Desktop\MediLink Tunisia\n8n-start.bat`** : launcher avec `N8N_WEBHOOK_URL` défini
- **`~/.n8n/database.sqlite`** : tables workflow/credential
- **`~/.n8n/n8nEventLog.log`** : logs exécutions
- **`Backend/prescription-service/.../service/PrescriptionService.java`** : `notifyN8n()` avec `N8N_WEBHOOK_PATHS` map
- **`Backend/prescription-service/src/main/resources/application.yml`** : `n8n.webhook.base-url`
