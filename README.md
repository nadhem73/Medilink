# MediLink Tunisia

> **Plateforme de santé connectée** — Mise en relation des patients, médecins, pharmacies et laboratoires en Tunisie.

| Aperçu | Scope |
|--------|-------|
| **Frontend** | Angular 18 (Administration, Médecins, Patients, Pharmacies) |
| **Mobile** | React Native / Expo 54 (Patients) |
| **Backend** | Spring Boot 3.2.0 — 10 microservices Java 21 |
| **IA** | Python FastAPI (Gemini Flash Vision + Tesseract OCR, LLM Chat, RAG) |
| **Messagerie** | Telegram Bot via n8n 2.8.4 |
| **Monitoring** | Prometheus + Grafana (Docker) |
| **CI/CD** | GitHub Actions (3 pipelines) |

---

## Table des matières

- [Architecture](#architecture)
- [Services Backend](#services-backend)
- [Frontend (Angular)](#frontend-angular)
- [Mobile App (Expo / React Native)](#mobile-app-expo--react-native)
- [AI Service](#ai-service)
- [Notification Telegram (n8n)](#notification-telegram-n8n)
- [Déploiement & Monitoring](#déploiement--monitoring)
- [Jeux de données](#jeux-de-données)
- [Prérequis](#prérequis)
- [Installation rapide](#installation-rapide)
- [Variables d'environnement](#variables-denvironnement)
- [Tests](#tests)
- [Structure du projet](#structure-du-projet)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (port 8765)                  │
│         Spring Cloud Gateway — JWT, rate-limit, circuit-breaker │
└──────────┬──────────┬──────────┬──────────┬──────────┬──────────┘
           │          │          │          │          │
     ┌─────▼──┐  ┌────▼───┐  ┌──▼────┐  ┌─▼──────┐  ┌▼─────────┐
     │ Auth   │  │Patient │  │Doctor │  │Pharmacy│  │Prescript.│
     │ :8084  │  │:8082   │  │:8083  │  │:8085   │  │:8086     │
     └────────┘  └────────┘  └───────┘  └────────┘  └────┬──────┘
           │          │          │          │             │
     ┌─────▼──┐  ┌────▼───┐  ┌──▼────┐  ┌─▼──────┐     │
     │ Bilan  │  │Monitor.│  │Config │  │Eureka  │     │
     │ :8087  │  │:8090   │  │:8888  │  │:8761   │     │
     └────────┘  └────────┘  └───────┘  └────────┘     │
                                                        │
     ┌───────────────────────────────────────────────────▼────┐
     │              AI Service (Python FastAPI :8093)         │
     │  OCR (Gemini Vision → Tesseract) + Chat LLM + RAG      │
     └────────────────────────────────────────────────────────┘
     ┌─────────────────────────────────────────────────────────┐
     │              n8n (port 5678) — Telegram Bot             │
     │   3 workflows : prescription-created → callback →       │
     │   prescription-prepared (pickup code)                   │
     └─────────────────────────────────────────────────────────┘
     ┌─────────────────────────────────────────────────────────┐
     │      Docker : Prometheus (:9090) + Grafana (:3000)      │
     └─────────────────────────────────────────────────────────┘
```

Chaque microservice possède **sa propre base PostgreSQL** et s'enregistre dans **Eureka** au démarrage. L'**API Gateway** filtre, route et sécurise toutes les requêtes (`/api/**`).

---

## Services Backend

| Service | Port | DB | Rôle |
|---------|------|----|------|
| **eureka-service** | 8761 | — | Annuaire des services (Netflix Eureka) |
| **config-server** | 8888 | — | Configuration centralisée (Git) |
| **api-gateway** | 8765 | — | Point d'entrée unique, JWT, rate-limiting, circuit breakers |
| **auth-service** | 8084 | `medilink_auth` | Authentification, JWT, rôles (Patient/Médecin/Pharmacien/Admin), email |
| **patient-service** | 8082 | `medilink_patients` | Dossiers médicaux, rendez-vous |
| **doctor-service** | 8083 | `medilink_doctors` | Profils médecins, disponibilités, consultations |
| **pharmacy-service** | 8085 | `medilink_pharmacy` | Pharmacies, catalogue médicaments, stock FIFO |
| **prescription-service** | 8086 | `medilink_prescriptions` | Ordonnances (cycle de vie complet), codes de retrait, webhooks n8n |
| **bilan-service** | 8087 | `medilink_bilan` | Analyses biologiques, OCR, partage médecin-patient |
| **monitoring-service** | 8090 | `medilink_monitoring` | Métriques temps réel (CPU/RAM/Disk), logs, sécurité, analytics, SSE, Prometheus |
| **ai-service** | 8093 | `medilink_ai` | (Python) OCR intelligente + Chat médical + RAG (ChromaDB) |

### Cycle de vie d'une ordonnance

```
BROUILLON → SOUMISE → EN_PREPARATION → PREPAREE → RETIREE → DISPENSEE → ARCHIVEE
                                                                       ↘ ANNULEE
```

À chaque transition clé, le `PrescriptionService` notifie n8n qui envoie un message Telegram au patient.

### FIFO Dispensation

Le `pharmacy-service` implémente un algorithme **FIFO** (`MedicationStockService.deduireStock()`) : les lots les plus anciens sont déduits en premier. Seuils de stock : `0 = rupture`, `≤10 = critique`, `≤50 = faible`, `>50 = suffisant`.

---

## Frontend (Angular)

**Stack** : Angular 18, Bootstrap 5.3, RxJS 7.8, ApexCharts, ECharts, Three.js

### Panneaux

| Panneau | Routes | Fonctionnalités principales |
|---------|--------|----------------------------|
| **Public** | `/` | Landing page |
| **Auth** | `/auth/*` | Login, register, forgot/reset password, vérification email |
| **Patient** | `/dashboard/patient/*` | Dossier médical, rendez-vous, ordonnances, bilans, messagerie |
| **Médecin** | `/dashboard/doctor/*` | Consultations, dossiers patients, ordonnances, téléconsultation |
| **Pharmacie** | `/dashboard/pharmacy/*` | Stock médicaments (FIFO), ordonnances, alertes rupture/péremption, prévisions |
| **Admin** | `/dashboard/admin/*` | Monitoring temps réel (CPU/RAM/Disk), logs, sécurité, analytics, utilisateurs |
| **Laboratoire** | `/dashboard/laboratory/*` | (Structure prête) |

### Monitoring (Panel Admin)

- **MonitoringOverviewComponent** : Gauges SVG CPU/RAM/Disk, flux SSE temps réel, historique temporel (30m à 30j), sélecteur de plage
- **LogExplorerComponent** : Recherche avec debounce, pagination, 4 filtres (service/niveau/date), export CSV
- **SecurityDashboardComponent** : 6 cartes sécurité, IPs suspectes, alertes avec accusé de réception
- **AnalyticsReportsComponent** : Superposition de métriques, statistiques récapitulatives, export CSV

---

## Mobile App (Expo / React Native)

**Stack** : React Native 0.81.5, Expo 54, Expo Router 6, NativeWind 4, Axios, AsyncStorage

### Navigation

```
(Root)
 ├── (auth)          → Login, Register
 └── (tabs)          → Bottom tabs (5)
      ├── Accueil    → Tableau de bord, rendez-vous, astuces santé
      ├── Rendez-vous→ Prise de rendez-vous (spécialités → médecins → créneaux)
      ├── Scan       ← Bouton central surélevé — Caméra / Galerie / Upload
      ├── Dossier    → Dossier médical, ordonnances, bilans
      └── Profil     → Informations personnelles, paramètres
```

### Fonctionnalités clés

| Fonctionnalité | Description |
|----------------|-------------|
| **Scan d'analyses** | Photo d'un bilan → OCR (Gemini Vision + fallback Tesseract) → Résultats structurés avec status Normal/Anormal/Critique |
| **Chat IA** | Assistant médical (Gemini) — extraction symptômes, urgence, spécialité recommandée, recherche de médecins, prise de RDV |
| **Rendez-vous** | Sélection spécialité → médecin → date (7 jours) → créneau (matin/après-midi/soir) |
| **Dossier médical** | Infos personnelles, constantes (taille/poids/IMC/groupe sanguin), allergies, traitements, ordonnances, bilans |
| **Télégram** | Notifications ordonnance créée, sélection pharmacie, code de retrait |

---

## AI Service

**Stack** : Python FastAPI, Google Gemini API, ChromaDB, Tesseract 5.5, SQLAlchemy async, asyncpg

### Modules

| Module | Endpoint | Description |
|--------|----------|-------------|
| **OCR** | `POST /api/ai/ocr` | Analyse de bilan médical (image → JSON structuré). Moteur primaire Gemini Flash Vision, fallback Tesseract + parser heuristique (fuzzy matching ≥ 0.72). |
| **Chat** | `POST /api/ai/chat` | Assistant médical conversationnel. Extraction symptômes/urgence/spécialité, RAG (ChromaDB), outils : `search_doctors`, `check_availability`, `book_appointment`. |
| **Health** | `GET /health` | Statut du service + modèle LLM utilisé. |

### OCR — Architecture

```
Image uploadée (base64)
       │
       ▼
GeminiVisionEngine (Gemini Flash Vision, response_mime_type: application/json)
       │
       ▼ (échec / quota épuisé)
TesseractEngine (preprocessing : grayscale + autocontrast + upscale 2× + median denoise)
       │
       ▼
BilanParser (extraction test+valeur+unité+référence, SequenceMatcher ≥ 0.72, aliases unités)
       │
       ▼
Résultat structuré avec flags NORMAL / ANORMAL / CRITIQUE
```

Moteurs alternatifs disponibles : `easyocr`, `paddleocr`, `grok`, `groq`.

---

## Notification Telegram (n8n)

Trois workflows n8n automatisent la communication avec le patient via Telegram.

### Workflow 1 : `prescription-created` (notify-prescription)

```
Ordonnance créée (SOUMISE)
       │
       ▼
Webhook POST → Code node → Récupère les pharmacies disponibles
       │
       ▼
Message Telegram au patient avec boutons inline (sélection pharmacie)
```

### Workflow 2 : `telegram-callback` (handle-callback)

```
Patient clique sur une pharmacie
       │
       ▼
Telegram Trigger (callback_query) → Parse callback_data (pick:{prescriptionId}:{pharmacyId})
       │
       ▼
Assignation pharmacie via backend → Confirmation au patient
```

### Workflow 3 : `prescription-prepared` (send-pickup-code)

```
Pharmacien prépare l'ordonnance (PREPAREE)
       │
       ▼
Webhook POST → Code node → Génère code 6 chiffres → Stocke via backend
       │
       ▼
Message Telegram au patient avec le code de retrait
```

### Proxy standalone

`telegram-callback-proxy.js` : serveur HTTP (port 3456) alternative au WF2 pour gérer les callbacks Telegram.

---

## Déploiement & Monitoring

### Docker Compose

```bash
docker-compose -f deploy/docker-compose.yml up -d
```

| Service | Port | Accès |
|---------|------|-------|
| **Prometheus** | 9090 | `http://localhost:9090` |
| **Grafana** | 3000 | `http://localhost:3000` (admin / medilink2025) |

Le `prometheus.yml` scrape les 11 services via `host.docker.internal:8765`.

### Métriques exposées

- **Monitoring Service** : `GET /api/monitoring/metrics/prometheus` (format texte Prometheus : cpu, ram, disk, response_time, requests, errors, info)
- **API Gateway** : `/actuator/prometheus` (Micrometer)
- **Historique persistant** : 30 jours de rétention dans PostgreSQL (nettoyage via cron toutes les 3h)

---

## Jeux de données

Des données Tunisiennes réelles sont fournies dans `/Datasets/` pour le seeding :

| Fichier | Description |
|---------|-------------|
| `medecins.csv` | Profils médecins (spécialités, villes, honoraires) |
| `pharmacies.csv` | Pharmacies (adresses, horaires, garde) |
| `medicine_data_tn.csv` | Catalogue médicaments (DCI, dosage, prix TND, remboursement) |
| `stock_medicaments_tn.csv` | Lots d'inventaire (3782 lignes) |

---

## Prérequis

| Outil | Version requise |
|-------|----------------|
| **Java** | 21 (Temurin) |
| **Maven** | 3.8+ |
| **Node.js** | 22+ |
| **Python** | 3.10+ |
| **PostgreSQL** | 15+ |
| **Tesseract OCR** | 5.5+ (pack français) |
| **Docker** | 24+ (pour Prometheus/Grafana) |
| **n8n** | 2.8.4 (installé globalement) |

---

## Installation rapide

### 1. Backend (Spring Boot)

```bash
cd Backend

# Lancer Eureka (obligatoire en premier)
cd eureka-service && mvn spring-boot:run

# Lancer Config Server
cd ../config-server && mvn spring-boot:run

# Lancer API Gateway
cd ../api-gateway && mvn spring-boot:run

# Lancer les services métier (dans n'importe quel ordre)
cd ../auth-service && mvn spring-boot:run
cd ../patient-service && mvn spring-boot:run
cd ../doctor-service && mvn spring-boot:run
cd ../pharmacy-service && mvn spring-boot:run
cd ../prescription-service && mvn spring-boot:run
cd ../bilan-service && mvn spring-boot:run
cd ../monitoring-service && mvn spring-boot:run
```

### 2. AI Service (Python)

```bash
cd Backend/ai-service
pip install -r requirements.txt
uvicorn app.main:app --port 8093
```

### 3. Frontend (Angular)

```bash
cd Frontend/medilink-angular
npm install
ng serve  # → http://localhost:4200
```

### 4. Mobile App (Expo)

```bash
cd "Mobile App"
npm install
npx expo start
```

### 5. n8n & Telegram

```bash
# Lancer n8n (le .bat configure N8N_WEBHOOK_URL avec cloudflared)
./n8n-start.bat

# Importer les 3 workflows depuis /n8n-workflows/
```

---

## Variables d'environnement

Fichier `.env` à la racine (IA uniquement) :

```env
LLM_PROVIDER=gemini
GEMINI_API_KEY=...
LLM_MODEL=gemini-2.0-flash
```

Fichier `Backend/ai-service/.env` :

```env
GEMINI_API_KEY=AQ....
LLM_MODEL=gemini-flash-lite-latest
OCR_ENGINE=gemini
TESSERACT_PATH=C:\Program Files\Tesseract-OCR\tesseract.exe
GEMINI_VISION_MODEL=gemini-flash-lite-latest
AI_SERVER_PORT=8093
```

Configuration des bases : chaque service Spring Boot configure sa propre base PostgreSQL dans `application.yml`. Par défaut :
- Hôte : `localhost:5432`
- User : `postgres`
- Password : `postgres`
- Nom : `medilink_{service}`

---

## Tests

| Module | Framework | Commande |
|--------|-----------|----------|
| Backend (Java) | JUnit 5 | `mvn test` |
| Frontend (Angular) | Jasmine / Karma | `ng test` |
| Mobile App | Jest | `npx jest` |
| AI Service | pytest | `pytest` |

---

## Structure du projet

```
MediLink Tunisia/
├── Backend/                    # 10 microservices Spring Boot + 1 Python
│   ├── pom.xml                 # POM agrégateur Maven (multi-module)
│   ├── ai-service/             # Python FastAPI (OCR + Chat + RAG)
│   ├── api-gateway/            # Spring Cloud Gateway
│   ├── auth-service/           # Authentification & rôles
│   ├── bilan-service/          # Analyses biologiques
│   ├── config-server/          # Configuration centralisée
│   ├── doctor-service/         # Médecins & consultations
│   ├── eureka-service/         # Service discovery
│   ├── monitoring-service/     # Monitoring, logs, sécurité
│   ├── patient-service/        # Patients & rendez-vous
│   ├── pharmacy-service/       # Pharmacies & stock FIFO
│   └── prescription-service/   # Ordonnances & webhooks n8n
├── Frontend/
│   └── medilink-angular/       # Application Angular 18
├── Mobile App/                  # Application mobile Expo/React Native
├── deploy/                      # Docker Compose (Prometheus + Grafana)
├── n8n-workflows/               # Workflows n8n (JSON + guide)
├── Datasets/                    # Données Tunisiennes de seed
├── scripts/                     # Utilitaires (images, stock)
├── chroma_db/                   # Base vectorielle ChromaDB (RAG)
├── bilan/                       # Échantillons de bilans (tests OCR)
├── PDF/                         # Documentation projet (CDC, charte)
├── .github/workflows/           # CI/CD (3 pipelines)
├── AGENTS.md                    # Session context (développement)
└── n8n-start.bat               # Lanceur n8n avec cloudflared
```

---

## CI/CD

Trois pipelines GitHub Actions :

| Pipeline | Déclencheur | Actions |
|----------|-------------|---------|
| **Backend CI** | `push` sur `Backend/**` | Build Maven (JDK 21), tests, upload JARs |
| **Frontend CI** | `push` sur `Frontend/**` | ESLint, build production, tests Karma |
| **Mobile CI** | `push` sur `Mobile App/**` | ESLint, tests Jest, TypeScript check |

---

## License

Projet interne — Smart Health Tunisia / MediLink Tunisia.
