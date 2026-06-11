# Ambulance Service - Smart Health Tunisia

Service de gestion des ambulances et des urgences médicales.

## Fonctionnalités Principales

### 🚑 Gestion des Ambulances
- CRUD complet des ambulances
- Géolocalisation GPS en temps réel
- Suivi du statut (disponible, en mission, indisponible)
- Gestion de l'équipement et de l'équipage
- Historique de maintenance

### 🆘 Gestion des Urgences
- Création d'appels d'urgence
- Classification par type et priorité
- Dispatch intelligent automatique
- Suivi en temps réel du statut
- Temps de réponse et métriques

### 📍 Géolocalisation
- Tracking GPS en temps réel
- Historique des positions
- Calcul de distance (formule de Haversine)
- Recherche des ambulances à proximité
- Estimation du temps d'arrivée (ETA)

### 🔔 Notifications Temps Réel
- WebSocket pour les mises à jour en direct
- Notifications sur changement de statut
- Suivi des positions en temps réel
- Alertes pour le dispatching

## Technologies

- **Framework**: Spring Boot 3.2.0
- **Java**: 21
- **Base de données**: PostgreSQL
- **Temps réel**: WebSocket (STOMP)
- **Sécurité**: Spring Security + JWT
- **API**: REST

## Architecture

```
ambulance-service/
├── model/
│   ├── entity/          # Entités JPA
│   ├── dto/             # Objets de transfert
│   └── enums/           # Énumérations
├── repository/          # Accès données
├── service/             # Logique métier
├── controller/          # Endpoints REST
├── config/              # Configuration
└── exception/           # Gestion erreurs
```

## API Endpoints

### Ambulances
- `GET /ambulances` - Liste toutes les ambulances
- `GET /ambulances/available` - Ambulances disponibles
- `GET /ambulances/{id}` - Détails d'une ambulance
- `POST /ambulances` - Créer une ambulance
- `PUT /ambulances/{id}` - Modifier une ambulance
- `PUT /ambulances/{id}/status` - Changer le statut
- `POST /ambulances/location` - Mettre à jour la position GPS
- `DELETE /ambulances/{id}` - Désactiver une ambulance

### Urgences
- `POST /emergencies` - Créer une urgence
- `GET /emergencies` - Liste toutes les urgences
- `GET /emergencies/active` - Urgences actives
- `GET /emergencies/{id}` - Détails d'une urgence
- `GET /emergencies/code/{code}` - Recherche par code
- `PUT /emergencies/{id}/assign` - Assigner une ambulance
- `PUT /emergencies/{id}/status` - Changer le statut
- `PUT /emergencies/{id}/notes` - Ajouter notes paramédicales
- `PUT /emergencies/{id}/hospital` - Définir hôpital destination

### WebSocket
- `/ws` - Point de connexion WebSocket
- `/topic/ambulance/{id}/location` - Position en temps réel
- `/topic/emergencies/new` - Nouvelles urgences
- `/topic/emergency/{id}/status` - Changements de statut
- `/topic/emergency/{id}/assigned` - Affectations

## Configuration

### Base de données
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ambulance_db
    username: postgres
    password: postgres
```

### Port
```yaml
server:
  port: 8085
  servlet:
    context-path: /api/ambulance
```

## Démarrage

### Prérequis
- Java 21
- PostgreSQL
- Maven

### Lancement
```bash
cd ambulance-service
mvn spring-boot:run
```

### Base de données
Créer la base de données :
```sql
CREATE DATABASE ambulance_db;
```

Les tables seront créées automatiquement au démarrage.

## Modèle de Données

### Ambulance
- Informations véhicule (matricule, modèle)
- Position GPS actuelle
- Station de base
- Équipement disponible
- Équipage (conducteur, paramédical)
- Statut et disponibilité

### Emergency
- Informations appelant
- Informations patient
- Type et priorité d'urgence
- Localisation précise
- Ambulance assignée
- Statut et timestamps
- Notes paramédicales
- Hôpital de destination

### AmbulanceLocation
- Historique des positions GPS
- Vitesse et direction
- Précision
- Lien avec mission

## Dispatch Intelligent

Le service inclut un système de dispatch automatique qui :
1. Trouve l'ambulance disponible la plus proche
2. Calcule la distance et le temps estimé
3. Assigne automatiquement l'ambulance
4. Met à jour les statuts
5. Envoie les notifications

## Calcul de Distance

Utilise la formule de Haversine pour calculer la distance entre deux points GPS.

## Notifications en Temps Réel

Les événements suivants déclenchent des notifications WebSocket :
- Nouvelle urgence
- Ambulance assignée
- Changement de statut
- Mise à jour position GPS
- Arrivée sur les lieux
- Transport vers hôpital

## Prochaines Améliorations

- [ ] Intégration Google Maps / OpenStreetMap
- [ ] Optimisation des trajets en temps réel
- [ ] Analytics et tableaux de bord
- [ ] Intégration avec les hôpitaux
- [ ] Application mobile pour les ambulanciers
- [ ] Système de communication voix
- [ ] Historique et rapports d'intervention

## Auteur

Smart Health Tunisia Team
