# Laboratory Service - Smart Health Tunisia

## Description

Service de gestion des laboratoires d'analyses médicales pour la plateforme Smart Health Tunisia. Ce microservice gère l'ensemble des fonctionnalités liées aux laboratoires selon le cahier des charges.

## Fonctionnalités Principales

### 📋 Selon Cahier des Charges (Section 6.5 Module Laboratoires)

#### ✅ Gestion des Laboratoires
- Profil complet du laboratoire
- Informations de licence et directeur
- Géolocalisation et horaires d'ouverture
- Services disponibles (prélèvement à domicile, analyses urgentes)
- Accréditations et spécialités

#### ✅ Gestion des Demandes d'Analyses
- Création de demandes d'analyses médicales
- Gestion des types d'analyses disponibles
- Prélèvement au laboratoire ou à domicile
- Priorités (normale, urgente, urgence)
- Suivi du statut en temps réel

#### ✅ Upload des Résultats
- Upload des résultats au format PDF securisé
- Archivage numérique avec traçabilité complète
- Validation par biologiste
- Interprétation et commentaires

#### ✅ Notifications Automatiques
- Notification au patient quand résultats prêts
- Notification au médecin prescripteur
- Partage sécurisé entre professionnels

#### ✅ Partage Sécurisé
- Contrôle d'accès granulaire
- Consentement patient
- Audit trail complet

## Architecture

### Technologies
- **Framework**: Spring Boot 3.2.0
- **Langage**: Java 21
- **Base de données**: PostgreSQL (medilink_laboratory)
- **ORM**: Spring Data JPA / Hibernate
- **Sécurité**: Spring Security + JWT
- **Discovery**: Eureka Client
- **Communication**: OpenFeign

### Entités Principales

1. **Laboratory**: Informations du laboratoire
2. **AnalysisType**: Référentiel des types d'analyses
3. **AnalysisRequest**: Demande d'analyse
4. **AnalysisItem**: Ligne de demande
5. **AnalysisResult**: Résultat d'analyse avec PDF

## API Endpoints

### Laboratoires
```
GET    /api/laboratory/laboratories              - Liste des laboratoires
POST   /api/laboratory/laboratories              - Créer un laboratoire
GET    /api/laboratory/laboratories/{id}         - Détails d'un laboratoire
PUT    /api/laboratory/laboratories/{id}         - Modifier un laboratoire
DELETE /api/laboratory/laboratories/{id}         - Supprimer un laboratoire
GET    /api/laboratory/laboratories/search       - Rechercher des laboratoires
GET    /api/laboratory/laboratories/nearby       - Laboratoires à proximité
GET    /api/laboratory/laboratories/home-collection - Avec prélèvement à domicile
GET    /api/laboratory/laboratories/urgent       - Analyses urgentes disponibles
```

### Demandes d'Analyses
```
GET    /api/laboratory/requests                  - Liste des demandes
POST   /api/laboratory/requests                  - Créer une demande
GET    /api/laboratory/requests/{id}             - Détails d'une demande
PUT    /api/laboratory/requests/{id}             - Mettre à jour le statut
GET    /api/laboratory/requests/pending          - Demandes en attente
GET    /api/laboratory/requests/patient/{id}     - Historique patient
GET    /api/laboratory/requests/ready            - Résultats prêts
```

### Résultats
```
GET    /api/laboratory/results/{id}              - Détails d'un résultat
POST   /api/laboratory/results                   - Créer un résultat
POST   /api/laboratory/results/upload            - Upload PDF
PUT    /api/laboratory/results/{id}/validate     - Valider un résultat
GET    /api/laboratory/results/request/{id}      - Résultats d'une demande
```

### Types d'Analyses
```
GET    /api/laboratory/analysis-types            - Liste des types
POST   /api/laboratory/analysis-types            - Créer un type (Admin)
GET    /api/laboratory/analysis-types/{id}       - Détails d'un type
GET    /api/laboratory/analysis-types/category/{category} - Par catégorie
GET    /api/laboratory/analysis-types/search     - Rechercher
```

## Configuration

### Variables d'Environnement

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=medilink_laboratory
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Server
SERVER_PORT=8086

# Eureka
EUREKA_SERVER=http://localhost:8761/eureka/

# JWT
JWT_SECRET=your-secret-key

# Upload
UPLOAD_DIR=./uploads/laboratory-results

# Active Profile
ACTIVE_PROFILE=dev
```

## Démarrage

### Prérequis
- Java 21+
- PostgreSQL 14+
- Maven 3.8+

### Installation

```bash
# Créer la base de données
createdb medilink_laboratory

# Compiler le projet
mvn clean install

# Démarrer le service
mvn spring-boot:run
```

Le service sera accessible sur `http://localhost:8086`

## Intégration avec les autres services

### Dépendances
- **auth-service**: Authentification et autorisation
- **patient-service**: Informations patients
- **doctor-service**: Informations médecins
- **prescription-service**: Ordonnances médicales
- **notification-service**: Alertes et notifications
- **file-service**: Stockage des PDF de résultats
- **payment-service**: Paiement des analyses

## Sécurité

- Authentification JWT obligatoire
- Contrôle d'accès basé sur les rôles (RBAC)
- Les biologistes ne peuvent accéder qu'aux données de leur laboratoire
- Chiffrement des données sensibles
- Upload de fichiers sécurisé (10MB max)
- Audit trail complet

## Catégories d'Analyses

- **Hématologie**: Numération formule sanguine, etc.
- **Biochimie**: Glycémie, cholestérol, etc.
- **Immunologie**: Tests immunologiques
- **Microbiologie**: Culture bactérienne, etc.
- **Sérologie**: Sérologies diverses
- **Hormones**: Dosages hormonaux
- **Toxicologie**: Dépistage toxicologique
- **Génétique**: Tests génétiques
- **Histopathologie**: Analyses tissulaires
- **Cytologie**: Analyses cellulaires
- **Analyse d'urine**: ECBU, etc.
- **Parasitologie**: Recherche de parasites
- **Virologie**: Tests virologiques

## Monitoring

- Health checks: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

## Auteur

Smart Health Tunisia Development Team

## Version

1.0.0
