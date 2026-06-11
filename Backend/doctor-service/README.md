# Doctor Service

Service de gestion des médecins pour SmartHealth Tunisia.

## 📋 Description

Le Doctor Service gère toutes les informations relatives aux médecins de la plateforme, incluant :
- Profil professionnel des médecins
- Spécialités médicales
- Formation et expérience
- Disponibilités et horaires
- Documents professionnels (licences, diplômes, etc.)

## 🚀 Port

- **Port**: 8083

## 💾 Base de données

- **Nom**: `medilink_doctors`
- **Type**: PostgreSQL
- **Port**: 5432

## 📦 Entités principales

### Doctor
Informations principales du médecin :
- Informations personnelles (nom, email, téléphone)
- Numéro de licence médicale
- Biographie
- Adresse du cabinet
- Statut (PENDING, ACTIVE, INACTIVE, SUSPENDED, REJECTED)
- Tarif de consultation
- Langues parlées

### DoctorSpecialty
Spécialités médicales du médecin :
- Nom de la spécialité
- Spécialité principale ou secondaire
- Années d'expérience dans la spécialité

### DoctorEducation
Formation académique :
- Institution
- Diplôme (MD, PhD, etc.)
- Dates de début et fin
- Pays

### DoctorExperience
Expérience professionnelle :
- Hôpital/Clinique
- Poste occupé
- Service
- Dates (avec indicateur pour poste actuel)

### DoctorAvailability
Horaires de disponibilité :
- Jour de la semaine
- Heures de début et fin
- Lieu (cabinet, téléconsultation)
- Statut disponible/indisponible

### DoctorDocument
Documents professionnels :
- Type (licence, diplôme, certificat, assurance, etc.)
- Fichier (nom, path, type MIME, taille)
- Date de téléchargement

## 🔐 Sécurité

- Authentification JWT
- Vérification des autorisations pour accès aux données
- Vérification obligatoire avant activation du compte médecin

## 🌐 Endpoints principaux (à implémenter)

```
POST   /api/doctors                    - Créer un profil médecin
GET    /api/doctors/{id}                - Obtenir un médecin par ID
GET    /api/doctors/user/{userId}       - Obtenir un médecin par userId
PUT    /api/doctors/{id}                - Mettre à jour un médecin
DELETE /api/doctors/{id}                - Supprimer un médecin

GET    /api/doctors                     - Liste des médecins (avec filtres)
GET    /api/doctors/search              - Recherche avancée
GET    /api/doctors/{id}/specialties    - Spécialités d'un médecin
GET    /api/doctors/{id}/availability   - Disponibilités d'un médecin
GET    /api/doctors/{id}/documents      - Documents d'un médecin

POST   /api/doctors/{id}/verify         - Vérifier un médecin (admin)
PUT    /api/doctors/{id}/status         - Changer le statut (admin)
```

## 📊 Statuts du médecin

- **PENDING**: En attente de vérification
- **ACTIVE**: Actif et vérifié
- **INACTIVE**: Temporairement inactif
- **SUSPENDED**: Suspendu par l'administrateur
- **REJECTED**: Candidature rejetée

## 🔄 Intégrations

- **auth-service**: Récupération des informations utilisateur
- **appointment-service**: Gestion des rendez-vous
- **patient-service**: Accès aux dossiers patients

## 🏗️ Technologies

- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Eureka Client (Service Discovery)
- OpenFeign (Communication inter-services)
- JWT (Authentification)
- Lombok

## 📝 Configuration requise

1. Créer la base de données PostgreSQL:
```sql
CREATE DATABASE medilink_doctors;
```

2. Les tables seront créées automatiquement au démarrage (ddl-auto: update)

3. Démarrer le service:
```bash
mvn spring-boot:run
```

## 🎯 Prochaines étapes

1. ✅ Modèle de données créé
2. ⏳ Implémenter les repositories
3. ⏳ Implémenter les services métier
4. ⏳ Implémenter les controllers REST
5. ⏳ Ajouter la validation des données
6. ⏳ Implémenter la gestion des fichiers
7. ⏳ Tests unitaires et d'intégration
8. ⏳ Documentation Swagger/OpenAPI
