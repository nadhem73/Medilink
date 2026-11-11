# MediLink Tunisia

> **Connected Healthcare Platform** — Connecting patients, doctors, pharmacies, and labs across Tunisia.

| Overview | Stack |
|----------|-------|
| **Frontend** | Angular 18 (Admin, Doctors, Patients, Pharmacies) |
| **Mobile** | React Native / Expo 54 (Patients) |
| **Backend** | Spring Boot 3.2.0 — 10 microservices (Java 21) |
| **AI** | Python FastAPI (Gemini Flash Vision + Tesseract OCR, LLM Chat, RAG) |
| **Messaging** | Telegram Bot via n8n 2.8.4 |
| **Monitoring** | Prometheus + Grafana (Docker) |
| **CI/CD** | GitHub Actions (3 pipelines) |

---

## Table of Contents

- [Architecture](#architecture)
- [Backend Services](#backend-services)
- [Frontend (Angular)](#frontend-angular)
- [Mobile App (Expo / React Native)](#mobile-app-expo--react-native)
- [AI Service](#ai-service)
- [Telegram Notification (n8n)](#telegram-notification-n8n)
- [Deployment & Monitoring](#deployment--monitoring)
- [Datasets](#datasets)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Environment Variables](#environment-variables)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [CI/CD](#cicd)

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
     ┌──────────────────────────────────────────────────▼─────┐
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

Each microservice has **its own PostgreSQL database** and registers with **Eureka** on startup. The **API Gateway** filters, routes, and secures all requests (`/api/**`).

---

## Backend Services

| Service | Port | DB | Role |
|---------|------|----|------|
| **eureka-service** | 8761 | — | Service registry (Netflix Eureka) |
| **config-server** | 8888 | — | Centralized configuration (Git) |
| **api-gateway** | 8765 | — | Single entry point, JWT, rate-limiting, circuit breakers |
| **auth-service** | 8084 | `medilink_auth` | Authentication, JWT, roles (Patient/Doctor/Pharmacist/Admin), email |
| **patient-service** | 8082 | `medilink_patients` | Medical records, appointments |
| **doctor-service** | 8083 | `medilink_doctors` | Doctor profiles, availability, consultations |
| **pharmacy-service** | 8085 | `medilink_pharmacy` | Pharmacies, medication catalog, FIFO stock |
| **prescription-service** | 8086 | `medilink_prescriptions` | Prescriptions (full lifecycle), pickup codes, n8n webhooks |
| **bilan-service** | 8087 | `medilink_bilan` | Lab reports, OCR, doctor-patient sharing |
| **monitoring-service** | 8090 | `medilink_monitoring` | Real-time metrics (CPU/RAM/Disk), logs, security, analytics, SSE, Prometheus |
| **ai-service** | 8093 | `medilink_ai` | (Python) Intelligent OCR + Medical Chat + RAG (ChromaDB) |

### Prescription Lifecycle

```
DRAFT → SUBMITTED → IN_PREPARATION → PREPARED → COLLECTED → DISPENSED → ARCHIVED
                                                                        ↘ CANCELLED
```

At each key transition, `PrescriptionService` notifies n8n, which sends a Telegram message to the patient.

### FIFO Dispensation

The `pharmacy-service` implements a **FIFO** algorithm (`MedicationStockService.deduireStock()`): oldest lots are dispensed first. Stock thresholds: `0 = out of stock`, `≤10 = critical`, `≤50 = low`, `>50 = sufficient`.

---

## Frontend (Angular)

**Stack**: Angular 18, Bootstrap 5.3, RxJS 7.8, ApexCharts, ECharts, Three.js

### Panels

| Panel | Routes | Key Features |
|-------|--------|-------------|
| **Public** | `/` | Landing page |
| **Auth** | `/auth/*` | Login, register, forgot/reset password, email verification |
| **Patient** | `/dashboard/patient/*` | Medical record, appointments, prescriptions, lab reports, messaging |
| **Doctor** | `/dashboard/doctor/*` | Consultations, patient records, prescriptions, teleconsultation |
| **Pharmacy** | `/dashboard/pharmacy/*` | Stock management (FIFO), prescriptions, out-of-stock/expiry alerts, forecasting |
| **Admin** | `/dashboard/admin/*` | Real-time monitoring (CPU/RAM/Disk), logs, security, analytics, users |
| **Laboratory** | `/dashboard/laboratory/*` | (Structure ready) |

### Monitoring (Admin Panel)

- **MonitoringOverviewComponent**: SVG CPU/RAM/Disk gauges, SSE live stream, time range picker (30m to 30d), history charts
- **LogExplorerComponent**: Debounced search, pagination, 4 filters (service/level/date range), CSV export
- **SecurityDashboardComponent**: 6 security cards, suspicious IPs, alerts with acknowledge
- **AnalyticsReportsComponent**: Metric overlays, summary statistics, CSV export

---

## Mobile App (Expo / React Native)

**Stack**: React Native 0.81.5, Expo 54, Expo Router 6, NativeWind 4, Axios, AsyncStorage

### Navigation

```
(Root)
 ├── (auth)          → Login, Register
 └── (tabs)          → Bottom tabs (5)
      ├── Home       → Dashboard, appointments, health tips
      ├── Appointments → Book appointments (specialties → doctors → slots)
      ├── Scan       ← Elevated center button — Camera / Gallery / Upload
      ├── Records    → Medical dossier, prescriptions, lab reports
      └── Profile    → Personal info, settings
```

### Key Features

| Feature | Description |
|---------|-------------|
| **Lab Scan** | Photo a lab report → OCR (Gemini Vision + Tesseract fallback) → Structured results with Normal/Abnormal/Critical flags |
| **AI Chat** | Medical assistant (Gemini) — symptom extraction, urgency assessment, specialty recommendation, doctor search, appointment booking |
| **Appointments** | Select specialty → doctor → date (7 days) → slot (morning/afternoon/evening) |
| **Medical Dossier** | Personal info, vitals (height/weight/BMI/blood group), allergies, treatments, prescriptions, lab reports |
| **Telegram** | Notifications: prescription created, pharmacy selection, pickup code |

---

## AI Service

**Stack**: Python FastAPI, Google Gemini API, ChromaDB, Tesseract 5.5, SQLAlchemy async, asyncpg

### Modules

| Module | Endpoint | Description |
|--------|----------|-------------|
| **OCR** | `POST /api/ai/ocr` | Analyze lab report images (image → structured JSON). Primary: Gemini Flash Vision, fallback: Tesseract + heuristic parser (fuzzy matching ≥ 0.72). |
| **Chat** | `POST /api/ai/chat` | Conversational medical assistant. Extracts symptoms/urgency/specialty, RAG (ChromaDB), tools: `search_doctors`, `check_availability`, `book_appointment`. |
| **Health** | `GET /health` | Service status + active LLM model. |

### OCR Architecture

```
Uploaded image (base64)
       │
       ▼
GeminiVisionEngine (Gemini Flash Vision, response_mime_type: application/json)
       │
       ▼ (failure / quota exhausted)
TesseractEngine (preprocessing: grayscale + autocontrast + upscale 2× + median denoise)
       │
       ▼
BilanParser (extract test+value+unit+reference, SequenceMatcher ≥ 0.72, unit aliases)
       │
       ▼
Structured result with NORMAL / ABNORMAL / CRITICAL flags
```

Alternative engines available: `easyocr`, `paddleocr`, `grok`, `groq`.

---

## Telegram Notification (n8n)

Three n8n workflows automate patient communication via Telegram.

### Workflow 1: `prescription-created` (notify-prescription)

```
Prescription created (SUBMITTED)
       │
       ▼
Webhook POST → Code node → Fetch available pharmacies
       │
       ▼
Telegram message to patient with inline keyboard buttons (pharmacy selection)
```

### Workflow 2: `telegram-callback` (handle-callback)

```
Patient taps a pharmacy button
       │
       ▼
Telegram Trigger (callback_query) → Parse callback_data (pick:{prescriptionId}:{pharmacyId})
       │
       ▼
Assign pharmacy via backend → Confirm to patient
```

### Workflow 3: `prescription-prepared` (send-pickup-code)

```
Pharmacist prepares prescription (PREPARED)
       │
       ▼
Webhook POST → Code node → Generate 6-digit code → Store via backend
       │
       ▼
Telegram message to patient with pickup code
```

### Standalone Proxy

`telegram-callback-proxy.js`: HTTP server (port 3456) alternative to WF2 for handling Telegram callbacks.

---

## Deployment & Monitoring

### Docker Compose

```bash
docker-compose -f deploy/docker-compose.yml up -d
```

| Service | Port | Access |
|---------|------|--------|
| **Prometheus** | 9090 | `http://localhost:9090` |
| **Grafana** | 3000 | `http://localhost:3000` (admin / medilink2025) |

`prometheus.yml` scrapes all 11 services via `host.docker.internal:8765`.

### Exposed Metrics

- **Monitoring Service**: `GET /api/monitoring/metrics/prometheus` (Prometheus text format: cpu, ram, disk, response_time, requests, errors, info)
- **API Gateway**: `/actuator/prometheus` (Micrometer)
- **Persistent History**: 30-day retention in PostgreSQL (cleanup cron every 3h)

---

## Datasets

Real Tunisian data is provided in `/Datasets/` for seeding:

| File | Description |
|------|-------------|
| `medecins.csv` | Doctor profiles (specialties, cities, fees) |
| `pharmacies.csv` | Pharmacies (addresses, hours, on-call duty) |
| `medicine_data_tn.csv` | Medication catalog (DCI, dosage, TND price, reimbursement) |
| `stock_medicaments_tn.csv` | Inventory lots (3782 rows) |

---

## Prerequisites

| Tool | Required Version |
|------|------------------|
| **Java** | 21 (Temurin) |
| **Maven** | 3.8+ |
| **Node.js** | 22+ |
| **Python** | 3.10+ |
| **PostgreSQL** | 15+ |
| **Tesseract OCR** | 5.5+ (French language pack) |
| **Docker** | 24+ (for Prometheus/Grafana) |
| **n8n** | 2.8.4 (installed globally) |

---

## Quick Start

### 1. Backend (Spring Boot)

```bash
cd Backend

# Start Eureka (required first)
cd eureka-service && mvn spring-boot:run

# Start Config Server
cd ../config-server && mvn spring-boot:run

# Start API Gateway
cd ../api-gateway && mvn spring-boot:run

# Start business services (any order)
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
# Start n8n (the .bat sets N8N_WEBHOOK_URL with cloudflared)
./n8n-start.bat

# Import the 3 workflows from /n8n-workflows/
```

---

## Environment Variables

Root `.env` file (AI only):

```env
LLM_PROVIDER=gemini
GEMINI_API_KEY=...
LLM_MODEL=gemini-2.0-flash
```

`Backend/ai-service/.env`:

```env
GEMINI_API_KEY=AQ....
LLM_MODEL=gemini-flash-lite-latest
OCR_ENGINE=gemini
TESSERACT_PATH=C:\Program Files\Tesseract-OCR\tesseract.exe
GEMINI_VISION_MODEL=gemini-flash-lite-latest
AI_SERVER_PORT=8093
```

Database configuration: each Spring Boot service configures its own PostgreSQL database in `application.yml`. Defaults:
- Host: `localhost:5432`
- User: `postgres`
- Password: `postgres`
- DB name: `medilink_{service}`

---

## Testing

| Module | Framework | Command |
|--------|-----------|---------|
| Backend (Java) | JUnit 5 | `mvn test` |
| Frontend (Angular) | Jasmine / Karma | `ng test` |
| Mobile App | Jest | `npx jest` |
| AI Service | pytest | `pytest` |

---

## Project Structure

```
MediLink Tunisia/
├── Backend/                    # 10 Spring Boot microservices + 1 Python
│   ├── pom.xml                 # Maven aggregator POM (multi-module)
│   ├── ai-service/             # Python FastAPI (OCR + Chat + RAG)
│   ├── api-gateway/            # Spring Cloud Gateway
│   ├── auth-service/           # Authentication & roles
│   ├── bilan-service/          # Lab report management
│   ├── config-server/          # Centralized configuration
│   ├── doctor-service/         # Doctors & consultations
│   ├── eureka-service/         # Service discovery
│   ├── monitoring-service/     # Monitoring, logs, security
│   ├── patient-service/        # Patients & appointments
│   ├── pharmacy-service/       # Pharmacies & FIFO stock
│   └── prescription-service/   # Prescriptions & n8n webhooks
├── Frontend/
│   └── medilink-angular/       # Angular 18 web application
├── Mobile App/                  # Expo / React Native mobile app
├── deploy/                      # Docker Compose (Prometheus + Grafana)
├── n8n-workflows/               # n8n workflow JSON definitions + guide
├── Datasets/                    # Tunisian seed data
├── scripts/                     # Utility scripts (images, stock)
├── chroma_db/                   # ChromaDB vector store (RAG)
├── bilan/                       # Sample lab report images (OCR testing)
├── PDF/                         # Project documentation (specs, branding)
├── .github/workflows/           # CI/CD (3 pipelines)
├── AGENTS.md                    # Session context (development)
└── n8n-start.bat               # n8n launcher with cloudflared
```

---

## CI/CD

Three GitHub Actions pipelines:

| Pipeline | Trigger | Actions |
|----------|---------|---------|
| **Backend CI** | `push` on `Backend/**` | Maven build (JDK 21), tests, upload JARs |
| **Frontend CI** | `push` on `Frontend/**` | ESLint, production build, Karma tests |
| **Mobile CI** | `push` on `Mobile App/**` | ESLint, Jest tests, TypeScript check |

---

## License

Internal project — Smart Health Tunisia / MediLink Tunisia.
