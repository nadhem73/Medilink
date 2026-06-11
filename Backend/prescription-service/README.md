# Prescription Service

Service de gestion des ordonnances médicales électroniques pour Smart Health Tunisia.

## Description

Ce microservice gère l'ensemble du cycle de vie des ordonnances médicales :
- Création d'ordonnances par les médecins
- Consultation des ordonnances par les patients et médecins
- Dispensation des ordonnances par les pharmacies
- Suivi et historique des ordonnances

## Fonctionnalités Principales

### Pour les Médecins
- ✅ Créer des ordonnances électroniques avec médicaments
- ✅ Consulter l'historique des ordonnances créées
- ✅ Annuler une ordonnance non dispensée
- ✅ Ajouter diagnostic et notes médicales

### Pour les Patients
- ✅ Consulter leurs ordonnances actives et historiques
- ✅ Voir les détails des médicaments prescrits
- ✅ Vérifier le statut de dispensation

### Pour les Pharmacies
- ✅ Rechercher des ordonnances par numéro
- ✅ Dispenser des ordonnances
- ✅ Consulter l'historique des dispensations

## Modèle de Données

### Prescription
- `id`: Identifiant unique
- `prescriptionNumber`: Numéro d'ordonnance unique (RXxxxxxxx)
- `patientId`, `patientName`: Informations patient
- `doctorId`, `doctorName`, `doctorSpecialty`: Informations médecin
- `diagnosis`: Diagnostic
- `notes`: Notes additionnelles
- `status`: ACTIVE | DISPENSED | CANCELLED | EXPIRED
- `prescriptionDate`: Date de prescription
- `expiryDate`: Date d'expiration (par défaut 30 jours)
- `dispensedDate`: Date de dispensation
- `pharmacyId`, `pharmacyName`: Pharmacie ayant dispensé

### PrescriptionMedication
- `medicationName`: Nom du médicament
- `dosage`: Posologie (ex: "500mg")
- `frequency`: Fréquence (ONCE_DAILY, TWICE_DAILY, THREE_TIMES_DAILY, etc.)
- `duration`: Durée du traitement en jours
- `quantity`: Quantité à délivrer
- `instructions`: Instructions spécifiques

## API Endpoints

### Création et Gestion

```
POST   /api/prescriptions                  - Créer une ordonnance (DOCTOR)
GET    /api/prescriptions/{id}             - Obtenir une ordonnance par ID
GET    /api/prescriptions/number/{number}  - Obtenir par numéro
DELETE /api/prescriptions/{id}/cancel      - Annuler une ordonnance (DOCTOR)
```

### Consultation

```
GET    /api/prescriptions/patient/{patientId}         - Ordonnances d'un patient
GET    /api/prescriptions/patient/{patientId}/active  - Ordonnances actives
GET    /api/prescriptions/doctor/{doctorId}           - Ordonnances d'un médecin
GET    /api/prescriptions/search                      - Recherche avec filtres
```

### Dispensation

```
POST   /api/prescriptions/{id}/dispense    - Dispenser une ordonnance (PHARMACY)
GET    /api/prescriptions/pharmacy/{pharmacyId}  - Historique pharmacie
```

## Configuration

### Variables d'Environnement

```properties
# Service
SERVER_PORT=8085
SPRING_APPLICATION_NAME=prescription-service

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/prescription_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/

# JWT
JWT_SECRET=your-secret-key
```

## Technologies

- **Spring Boot 3.2.0** - Framework principal
- **Spring Data JPA** - Persistence
- **PostgreSQL** - Base de données
- **Spring Security** - Sécurité et authentification
- **Eureka Client** - Service discovery
- **Lombok** - Réduction du code boilerplate

## Sécurité

### Rôles et Permissions

| Endpoint | PATIENT | DOCTOR | PHARMACY | ADMIN |
|----------|---------|--------|----------|-------|
| POST /prescriptions | ❌ | ✅ | ❌ | ✅ |
| GET /prescriptions/{id} | ✅* | ✅* | ✅ | ✅ |
| GET /patient/{id} | ✅* | ✅ | ❌ | ✅ |
| GET /doctor/{id} | ❌ | ✅* | ❌ | ✅ |
| POST /{id}/dispense | ❌ | ❌ | ✅ | ✅ |
| DELETE /{id}/cancel | ❌ | ✅* | ❌ | ✅ |

*\* Uniquement pour leurs propres données*

### Authentification

Toutes les requêtes doivent inclure un JWT token valide dans le header :
```
Authorization: Bearer <token>
```

## Validation des Ordonnances

### Règles de Validation

1. **Expiration**: Les ordonnances expirent par défaut après 30 jours
2. **Dispensation**: Une ordonnance ne peut être dispensée qu'une seule fois
3. **Annulation**: Seul le médecin prescripteur peut annuler une ordonnance non dispensée
4. **Médicaments**: Au moins un médicament doit être prescrit

### Statuts

- **ACTIVE**: Ordonnance créée, non dispensée, non expirée
- **DISPENSED**: Ordonnance délivrée par une pharmacie
- **CANCELLED**: Ordonnance annulée par le médecin
- **EXPIRED**: Ordonnance expirée (date dépassée)

## Exemples d'Utilisation

### Créer une Ordonnance

```json
POST /api/prescriptions
{
  "patientId": 123,
  "patientName": "Ahmed Ben Ali",
  "doctorSpecialty": "Médecine Générale",
  "diagnosis": "Infection respiratoire",
  "notes": "À prendre après les repas",
  "expiryDate": "2024-02-15T23:59:59",
  "medications": [
    {
      "medicationName": "Amoxicilline",
      "dosage": "500mg",
      "frequency": "THREE_TIMES_DAILY",
      "duration": 7,
      "quantity": 21,
      "instructions": "Prendre avec un grand verre d'eau"
    }
  ]
}
```

### Dispenser une Ordonnance

```json
POST /api/prescriptions/123/dispense
{
  "notes": "Délivré complet"
}
```

## Tests

```bash
# Lancer les tests
./mvnw test

# Lancer les tests d'intégration
./mvnw verify
```

## Build et Déploiement

```bash
# Build
./mvnw clean package

# Run local
./mvnw spring-boot:run

# Docker
docker build -t prescription-service .
docker run -p 8085:8085 prescription-service
```

## Monitoring

Le service expose les endpoints suivants :
- `/actuator/health` - État de santé
- `/actuator/metrics` - Métriques
- `/actuator/info` - Informations sur le service

## Logs

Les logs sont configurés avec SLF4J et Logback :
- Niveau INFO par défaut
- Logs applicatifs dans `logs/prescription-service.log`
- Rotation quotidienne avec rétention de 30 jours

## Architecture

```
prescription-service/
├── config/          # Configuration Spring Security, CORS
├── controller/      # REST Controllers
├── service/         # Business Logic
├── repository/      # JPA Repositories
├── model/
│   ├── entity/      # JPA Entities
│   ├── dto/         # Data Transfer Objects
│   └── enums/       # Enumerations
├── exception/       # Custom Exceptions & Handlers
└── security/        # JWT Filter & Security Config
```

## Dépendances Inter-Services

- **Auth Service**: Validation des tokens JWT
- **Doctor Service**: Vérification de l'identité des médecins
- **Patient Service**: Vérification de l'identité des patients
- **Pharmacy Service**: Information sur les pharmacies

## Améliorations Futures

- [ ] Notifications automatiques avant expiration
- [ ] OCR pour numériser les ordonnances papier
- [ ] Intégration avec les bases de médicaments
- [ ] Vérification des interactions médicamenteuses
- [ ] Export PDF des ordonnances
- [ ] Signature électronique des ordonnances
- [ ] Statistiques et analytics
- [ ] Support multi-langues (arabe, français)

## Contact & Support

Pour toute question ou problème, consultez la documentation complète ou contactez l'équipe de développement.
