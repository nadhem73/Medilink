# Pharmacy Service - Smart Health Tunisia

## Description

Service de gestion des pharmacies pour la plateforme Smart Health Tunisia. Ce microservice gère l'ensemble des fonctionnalités liées aux pharmacies selon le cahier des charges.

## Fonctionnalités Principales

### 📋 Selon Cahier des Charges (Section 6.4 Module Pharmacies)

#### ✅ Gestion des Pharmacies
- Profil complet de la pharmacie
- Informations de licence et propriétaire
- Géolocalisation et horaires d'ouverture
- Services disponibles (livraison à domicile, service de nuit)

#### ✅ Gestion des Stocks de Médicaments
- Référentiel centralisé des médicaments
- Suivi en temps réel des quantités
- Niveaux de stock (minimum, réapprovisionnement, maximum)
- Alertes automatiques pour stocks faibles
- Suivi des dates d'expiration
- Localisation des médicaments dans la pharmacie

#### ✅ Ordonnances Électroniques
- Réception des ordonnances en temps réel
- Vérification de disponibilité des médicaments
- Dispensation et traçabilité complète
- Substitution de médicaments si nécessaire
- Instructions au patient

#### ✅ Gestion des Commandes Fournisseurs
- Création de commandes de réapprovisionnement
- Suivi du statut des commandes
- Gestion des livraisons
- Historique complet

#### ✅ Alertes Automatiques
- Rupture de stock
- Stock faible nécessitant réapprovisionnement
- Médicaments arrivant à expiration
- Médicaments expirés

## Architecture

### Technologies
- **Framework**: Spring Boot 3.2.0
- **Langage**: Java 21
- **Base de données**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Sécurité**: Spring Security + JWT
- **Discovery**: Eureka Client
- **Communication**: OpenFeign

### Entités Principales

1. **Pharmacy**: Informations de la pharmacie
2. **Medication**: Référentiel des médicaments
3. **MedicationStock**: Stock par pharmacie
4. **PrescriptionDispensation**: Dispensation d'ordonnances
5. **PharmacyOrder**: Commandes de réapprovisionnement

## API Endpoints

### Pharmacies
```
GET    /api/pharmacy/pharmacies              - Liste des pharmacies
POST   /api/pharmacy/pharmacies              - Créer une pharmacie
GET    /api/pharmacy/pharmacies/{id}         - Détails d'une pharmacie
PUT    /api/pharmacy/pharmacies/{id}         - Modifier une pharmacie
DELETE /api/pharmacy/pharmacies/{id}         - Supprimer une pharmacie
GET    /api/pharmacy/pharmacies/search       - Rechercher des pharmacies
GET    /api/pharmacy/pharmacies/nearby       - Pharmacies à proximité (géolocalisation)
GET    /api/pharmacy/pharmacies/night-service - Pharmacies de garde
```

### Stocks
```
GET    /api/pharmacy/stocks                  - Liste des stocks
POST   /api/pharmacy/stocks                  - Ajouter un médicament au stock
GET    /api/pharmacy/stocks/{id}             - Détails d'un stock
PUT    /api/pharmacy/stocks/{id}             - Mettre à jour le stock
GET    /api/pharmacy/stocks/low              - Stocks faibles
GET    /api/pharmacy/stocks/critical         - Stocks critiques
GET    /api/pharmacy/stocks/expiring         - Médicaments arrivant à expiration
GET    /api/pharmacy/stocks/search           - Rechercher dans le stock
```

### Médicaments
```
GET    /api/pharmacy/medications             - Liste des médicaments
POST   /api/pharmacy/medications             - Ajouter un médicament (Admin)
GET    /api/pharmacy/medications/{id}        - Détails d'un médicament
PUT    /api/pharmacy/medications/{id}        - Modifier un médicament (Admin)
GET    /api/pharmacy/medications/search      - Rechercher des médicaments
```

### Ordonnances
```
GET    /api/pharmacy/dispensations           - Liste des dispensations
POST   /api/pharmacy/dispensations           - Créer une dispensation
GET    /api/pharmacy/dispensations/{id}      - Détails d'une dispensation
PUT    /api/pharmacy/dispensations/{id}      - Mettre à jour le statut
GET    /api/pharmacy/dispensations/pending   - Dispensations en attente
GET    /api/pharmacy/dispensations/patient/{patientId} - Historique patient
```

### Commandes Fournisseurs
```
GET    /api/pharmacy/orders                  - Liste des commandes
POST   /api/pharmacy/orders                  - Créer une commande
GET    /api/pharmacy/orders/{id}             - Détails d'une commande
PUT    /api/pharmacy/orders/{id}             - Mettre à jour le statut
GET    /api/pharmacy/orders/active           - Commandes actives
```

## Configuration

### Variables d'Environnement

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=medilink_pharmacy
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Server
SERVER_PORT=8085

# Eureka
EUREKA_SERVER=http://localhost:8761/eureka/

# JWT
JWT_SECRET=your-secret-key

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
createdb medilink_pharmacy

# Compiler le projet
mvn clean install

# Démarrer le service
mvn spring-boot:run
```

Le service sera accessible sur `http://localhost:8085`

## Intégration avec les autres services

### Dépendances
- **auth-service**: Authentification et autorisation
- **prescription-service**: Réception des ordonnances électroniques
- **patient-service**: Informations patients
- **doctor-service**: Informations médecins
- **notification-service**: Alertes et notifications
- **payment-service**: Paiement des médicaments

## Sécurité

- Authentification JWT obligatoire
- Contrôle d'accès basé sur les rôles (RBAC)
- Les pharmaciens ne peuvent accéder qu'aux données de leur pharmacie
- Chiffrement des données sensibles
- Audit trail complet

## Monitoring

- Health checks: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

## Auteur

Smart Health Tunisia Development Team

## Version

1.0.0
