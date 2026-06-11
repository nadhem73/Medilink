# 🌐 Smart Health Tunisia - API Gateway

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-2023.0.0-blue.svg)](https://spring.io/projects/spring-cloud-gateway)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

L'API Gateway est le point d'entrée unique pour tous les microservices de Smart Health Tunisia. Elle gère le routage, l'authentification, la sécurité, le rate limiting et le monitoring de toutes les requêtes.

## ✨ Fonctionnalités

### 🔐 Sécurité
- **Authentification JWT** : Validation centralisée des tokens
- **Autorisation basée sur les rôles** : ADMIN, DOCTOR, PATIENT, etc.
- **CORS configuré** : Support pour les applications frontend
- **Headers de sécurité** : Propagation des informations utilisateur aux microservices

### 🔄 Routage Intelligent
- **Service Discovery** : Intégration avec Eureka
- **Load Balancing** : Distribution automatique des requêtes
- **Circuit Breaker** : Protection contre les pannes en cascade avec Resilience4j
- **Retry Logic** : Tentatives automatiques en cas d'échec

### 🚦 Rate Limiting
- **Limitation par IP** : 100 requêtes/minute par défaut
- **Backend Redis** : Stockage distribué des compteurs
- **Headers informatifs** : Indication du temps d'attente

### 📊 Monitoring & Observabilité
- **Logging centralisé** : Toutes les requêtes/réponses
- **Métriques Prometheus** : Export des métriques de performance
- **Actuator endpoints** : Health checks et informations système
- **Request tracking** : Temps de réponse et statuts

### 🛡️ Résilience
- **Fallback endpoints** : Réponses gracieuses si un service est down
- **Timeout configuration** : Prévention des requêtes bloquées
- **Connection pooling** : Gestion efficace des connexions

## 🏗️ Architecture

```
┌─────────────┐
│   Clients   │
│ (Web/Mobile)│
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│           API Gateway :8080              │
│  ┌──────────────────────────────────┐   │
│  │  Security Layer (JWT)            │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Rate Limiting (Redis)           │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Routing & Load Balancing        │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Circuit Breaker                 │   │
│  └──────────────────────────────────┘   │
└─────────────────┬───────────────────────┘
                  │
        ┌─────────┴──────────┐
        ▼                    ▼
┌───────────────┐    ┌───────────────┐
│ Eureka Server │    │ Microservices │
│    :8761      │◄───┤   (Various)   │
└───────────────┘    └───────────────┘
```

## 🚀 Démarrage Rapide

### Prérequis
- Java 21+
- Maven 3.8+
- Redis (pour rate limiting)
- Eureka Server (pour service discovery)

### Configuration

1. **Cloner le projet**
```bash
git clone <repository-url>
cd Backend/api-gateway
```

2. **Configuration de l'environnement**

Créer un fichier `.env` (optionnel) :
```env
SPRING_PROFILES_ACTIVE=dev
EUREKA_URI=http://localhost:8761/eureka/
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your-super-secret-key
```

3. **Démarrer Redis** (si pas déjà en cours d'exécution)
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

4. **Compiler et exécuter**
```bash
# Compilation
mvn clean package

# Exécution
mvn spring-boot:run

# Ou avec le JAR
java -jar target/api-gateway-1.0.0.jar
```

### Avec Docker

```bash
# Build de l'image
docker build -t smart-health-api-gateway .

# Démarrer avec docker-compose (inclut Redis)
docker-compose up -d
```

## 📍 Endpoints Principaux

### Gateway Info
- **GET /** : Informations sur la gateway
- **GET /health** : Health check
- **GET /services** : Liste des services enregistrés

### Actuator (Monitoring)
- **GET /actuator/health** : Santé de l'application
- **GET /actuator/metrics** : Métriques de performance
- **GET /actuator/prometheus** : Métriques Prometheus
- **GET /actuator/gateway/routes** : Routes configurées

### Services Routés

Tous les microservices sont accessibles via `/api/{service-name}/...`

| Service | Route | Description |
|---------|-------|-------------|
| Auth | `/api/auth/**` | Authentification et autorisation |
| Patient | `/api/patients/**` | Gestion des patients |
| Doctor | `/api/doctors/**` | Gestion des médecins |
| Appointment | `/api/appointments/**` | Rendez-vous médicaux |
| Prescription | `/api/prescriptions/**` | Ordonnances |
| Pharmacy | `/api/pharmacies/**` | Pharmacies |
| Laboratory | `/api/laboratories/**` | Laboratoires |
| Ambulance | `/api/ambulances/**` | Services d'ambulance |
| Teleconsultation | `/api/teleconsultations/**` | Téléconsultations |
| Payment | `/api/payments/**` | Paiements |
| Notification | `/api/notifications/**` | Notifications |
| File | `/api/files/**` | Gestion des fichiers |
| Geolocation | `/api/geolocation/**` | Géolocalisation |
| Analytics | `/api/analytics/**` | Analytique |
| AI | `/api/ai/**` | Intelligence artificielle |
| Admin | `/api/admin/**` | Administration |

## 🔑 Authentification

### Endpoints Publics (sans authentification)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/auth/verify-email/**`
- `GET /api/doctors/search`
- `GET /api/pharmacies/nearby`

### Endpoints Authentifiés

Pour accéder aux endpoints protégés, incluez le token JWT dans le header :

```bash
Authorization: Bearer <your-jwt-token>
```

La gateway extrait les informations du token et les propage aux microservices via les headers :
- `X-User-Id` : ID de l'utilisateur
- `X-User-Username` : Nom d'utilisateur
- `X-User-Roles` : Rôles de l'utilisateur (séparés par des virgules)

## ⚙️ Configuration

### Profils Spring

- **dev** : Développement local
- **test** : Tests unitaires/intégration
- **prod** : Production

### Variables d'Environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `SERVER_PORT` | Port du serveur | 8080 |
| `EUREKA_URI` | URL d'Eureka | http://localhost:8761/eureka/ |
| `REDIS_HOST` | Hôte Redis | localhost |
| `REDIS_PORT` | Port Redis | 6379 |
| `REDIS_PASSWORD` | Mot de passe Redis | (vide) |
| `JWT_SECRET` | Clé secrète JWT | (voir config) |
| `JWT_EXPIRATION` | Durée de validité JWT (ms) | 86400000 (24h) |

### Rate Limiting

Par défaut : **100 requêtes par minute par IP**

Modifiable dans `application.yml` ou via la classe `RateLimitFilter.java`

### Circuit Breaker

Configuration Resilience4j :
- **Sliding window** : 10 appels
- **Failure threshold** : 50%
- **Wait duration** : 10 secondes
- **Half-open calls** : 3

## 🧪 Tests

```bash
# Exécuter tous les tests
mvn test

# Tests avec couverture
mvn test jacoco:report
```

## 📊 Monitoring

### Métriques Prometheus

Accédez aux métriques via :
```
http://localhost:8080/actuator/prometheus
```

Métriques disponibles :
- Nombre de requêtes par endpoint
- Temps de réponse
- Taux d'erreur
- Circuit breaker status
- Connection pool metrics

### Logs

Les logs sont disponibles dans :
- Console (développement)
- `/var/log/api-gateway/application.log` (production)

Format de log :
```
2026-06-02 14:22:01 - 🔵 Incoming Request: GET /api/doctors/search from 192.168.1.1
2026-06-02 14:22:01 - 🟢 Response: /api/doctors/search - Status: 200 - Duration: 123ms
```

## 🐛 Dépannage

### Redis Connection Error
```bash
# Vérifier si Redis est en cours d'exécution
redis-cli ping
# Devrait retourner "PONG"
```

### Service Not Found (404)
1. Vérifier qu'Eureka Server est accessible
2. Vérifier que le microservice est enregistré dans Eureka
3. Vérifier les logs de la gateway

### JWT Validation Failed
1. Vérifier que `JWT_SECRET` est identique entre auth-service et api-gateway
2. Vérifier que le token n'est pas expiré
3. Vérifier le format du header Authorization

### Rate Limit Issues
```bash
# Vérifier les clés Redis
redis-cli keys "rate_limit:*"
```

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 License

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 👥 Équipe

Smart Health Tunisia Development Team

## 📞 Support

Pour toute question ou problème :
- Email: support@medilinktunisia.com
- Documentation: https://docs.medilinktunisia.com
- Issues: GitHub Issues

---

**Made with ❤️ by Smart Health Tunisia Team**
