# MediLink Tunisia – Session Context

## Goal
Finaliser le flux notifications Telegram patient via n8n. Ajouter la recherche/filtres sur la page ordonnances pharmacie. Terminer l'IA et la navigation mobile.

## Constraints & Preferences
- **Gratuit** : Telegram Bot API uniquement.
- **n8n 2.8.4** installé via npm, tourne sur `http://localhost:5678`.
- **Bot Telegram** : `@medilink_tunisie_bot` (token `8644047281:AAHywXMMM9zpDThyBj_FOgme46RZi_GhzRk`).

## Progress
### ✅ Done
- **Migration Drawer → Bottom Tabs (Mobile)** : Remplacement du menu Drawer par une barre d'onglets en bas avec 5 tabs (Accueil, Rendez-vous, Scan, Dossier, Profil). Bouton Scan central surélevé. Écrans existants réutilisés via réexportation. Navigation secondaire (ai-chat, explore, settings, modal) conservée dans un Stack `(drawer)`. `CustomDrawer.tsx` supprimé. Build OK.
- **Correction AI Service** : Gemini Flash Lite, Eureka init async, HS384 JWT, auth-service `/api/auth/doctors` public, search_doctors tool avec filtrage spécialité/ville, mapping spécialités.
- **Recherche + Filtres (Pharmacy) — redesign** : Barre de recherche avec icône SVG, bouton tri avec libellé (Récent/Ancien), filtres par statut en pillules cliquables (remplace le `<select>`), état vide filtré avec bouton "Effacer les filtres". Layout en 3 zones distinctes (header/search+pills/liste). Build OK.
- **FIFO dispensation backend** : `POST /stock/dispenser` endpoint, `PrescriptionService.deduireStock()` on `DISPENSEE`, `quantitePrescrite` added to DTO, 16 JUnit tests for `MedicationStockServiceTest` + 14 for `MedicationStockControllerTest`.
- **FIFO dispensation frontend tests** : Fixed 3 flaky stock component tests — corrected `getStockStatus` thresholds (0→rupture, ≤10→critique, ≤50→faible, >50→suffisant), aligned `priceRange` expectations, fixed `nextPage` page boundary assertions. All 33 stock tests now pass.
- **Alerts component tests** : 19/21 pass (2 intermittent failures due to Jasmine shared spy state with `throwError`/`of`).

### ✅ Bilan OCR — Nouveau microservice bilan-service
- **bilan-service** (Spring Boot, port 8087) créé : entities JPA (Bilan, BilanResult), Controller REST (`/api/bilans/scan`, GET, PUT confirm, DELETE), Security JWT, OcrClientService→AI Service, ImageStorageService.
- **AI Service** — Nouvel endpoint `POST /api/ai/ocr` : utilise Gemini 2.0 Flash Vision pour extraire les données des bilans scannés (détection auto du type et du format nouveau/ancien patient).
- **Mobile App** — `scan.tsx` câblé avec caméra/galerie (expo-image-picker), `scan-result.tsx` pour preview+confirmation, `bilanService.ts`.
- **API Gateway** — Route `/api/bilans/**` ajoutée vers `lb://BILAN-SERVICE`.
- **⚠️ Limitation** : Gemini API free tier a un quota limité (daily requests). Si `429 RESOURCE_EXHAUSTED`, attendre ou changer de clé API dans `ai-service/.env`.

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
- **`Mobile App/app/(tabs)/_layout.tsx`** : Bottom tab navigator (5 tabs, scan central surélevé)
- **`Mobile App/app/(tabs)/scan.tsx`** : Écran Scan (caméra/galerie/upload via bilanService)
- **`Mobile App/app/(tabs)/scan-result.tsx`** : Preview des données OCR avec confirmation
- **`Mobile App/src/services/bilanService.ts`** : Service API pour le scan de bilans
- **`Backend/bilan-service/...`** : Microservice complet (controller, service, JPA, security)
- **`Mobile App/app/(tabs)/home.tsx`** : Réexport de `(drawer)/home`
- **`Mobile App/app/(drawer)/home.tsx`** : Navigation Drawer → router (ai-chat remplace hamburger)
- **`Mobile App/app/(drawer)/_layout.tsx`** : Stack navigator (secondaires : ai-chat, explore, settings, modal)
