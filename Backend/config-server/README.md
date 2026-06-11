# ⚙️ Smart Health Tunisia - Config Server

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud Config](https://img.shields.io/badge/Spring%20Cloud%20Config-2023.0.0-blue.svg)](https://spring.io/projects/spring-cloud-config)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

Le Config Server est le serveur de configuration centralisé pour tous les microservices de Smart Health Tunisia. Il permet de gérer et de distribuer les configurations de manière centralisée, sécurisée et versionnée.

## ✨ Fonctionnalités

### 🔐 Gestion Centralisée
- **Configuration unique** : Toutes les configurations au même endroit
- **Environnements multiples** : dev, test, prod
- **Rechargement dynamique** : Mise à jour sans redémarrage
- **Sécurité** : Authentification HTTP Basic

### 📦 Backend de Stockage
- **Native** : Fichiers locaux (classpath ou filesystem)
- **Git** : Repository Git pour versioning et collaboration
- **Support multi-sources** : Basculement facile entre les backends

### 🔄 Intégration
- **Service Discovery** : Enregistrement dans Eureka
- **Actuator** : Health checks et métriques
- **Spring Cloud Bus** : Propagation des changements (optionnel)

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│         Config Server :8888              │
│  ┌──────────────────────────────────┐   │
│  │  Security Layer (Basic Auth)     │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │  Configuration Repository        │   │
│  │  - Native (Local Files)          │   │
│  │  - Git (Remote Repository)       │   │
│  └──────────────────────────────────┘   │
└─────────────────┬───────────────────────┘
                  │
        ┌─────────┴──────────┐
        ▼                    ▼
┌───────────────┐    ┌───────────────┐
│ Microservice  │    │ Microservice  │
│   (Client)    │    │   (Client)    │
└───────────────┘    └───────────────┘
```

## 🚀 Démarrage Rapide

### Prérequis
- Java 21+
- Maven 3.8+
- Eureka Server (pour service discovery)

### Installation

1. **Cloner le projet**
```bash
git clone <repository-url>
cd Backend/config-server
```

2. **Configuration**

Créer un fichier `.env` (optionnel) :
```env
SPRING_PROFILES_ACTIVE=native
CONFIG_SERVER_USERNAME=configadmin
CONFIG_SERVER_PASSWORD=configadmin123
EUREKA_URI=http://localhost:8761/eureka/
```

3. **Compiler et exécuter**
```bash
# Compilation
mvn clean package

# Exécution
mvn spring-boot:run

# Ou avec le JAR
java -jar target/config-server-1.0.0.jar
```

### Avec Docker

```bash
# Build de l'image
docker build -t smart-health-config-server .

# Démarrer avec docker-compose
docker-compose up -d
```

## 📍 Endpoints Principaux

### Configuration Endpoints

Format d'URL pour récupérer les configurations :

```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.yml
```

#### Exemples

**Récupérer la configuration du service auth (profil dev)**
```bash
curl -u configadmin:configadmin123 \
  http://localhost:8888/auth-service/dev
```

**Récupérer la configuration de l'API Gateway (profil prod)**
```bash
curl -u configadmin:configadmin123 \
  http://localhost:8888/api-gateway/prod
```

**Format YAML**
```bash
curl -u configadmin:configadmin123 \
  http://localhost:8888/patient-service-dev.yml
```

**Format Properties**
```bash
curl -u configadmin:configadmin123 \
  http://localhost:8888/doctor-service-dev.properties
```

### Actuator Endpoints

- **GET /actuator/health** : Health check
- **GET /actuator/info** : Informations sur l'application
- **POST /actuator/refresh** : Rafraîchir la configuration
- **POST /actuator/bus-refresh** : Rafraîchir tous les clients (avec Spring Cloud Bus)

## 🗂️ Structure des Configurations

### Mode Native (Fichiers Locaux)

```
src/main/resources/
└── configurations/
    ├── auth-service.yml
    ├── patient-service.yml
    ├── doctor-service.yml
    ├── appointment-service.yml
    ├── payment-service.yml
    ├── notification-service.yml
    ├── file-service.yml
    └── api-gateway.yml
```

### Mode Git

Structure recommandée du repository Git :

```
config-repo/
├── auth-service/
│   ├── auth-service.yml
│   ├── auth-service-dev.yml
│   ├── auth-service-prod.yml
│   └── auth-service-test.yml
├── patient-service/
│   ├── patient-service.yml
│   └── ...
└── ...
```

## ⚙️ Configuration

### Profils Spring

- **native** : Utilise des fichiers locaux (développement)
- **git** : Utilise un repository Git (production)

### Variables d'Environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `SERVER_PORT` | Port du serveur | 8888 |
| `SPRING_PROFILE` | Profil actif | native |
| `CONFIG_SERVER_USERNAME` | Nom d'utilisateur | configadmin |
| `CONFIG_SERVER_PASSWORD` | Mot de passe | configadmin123 |
| `EUREKA_URI` | URL d'Eureka | http://localhost:8761/eureka/ |
| `CONFIG_GIT_URI` | URI du repo Git | - |
| `CONFIG_GIT_USERNAME` | Username Git | - |
| `CONFIG_GIT_PASSWORD` | Password/Token Git | - |

### Sécurité

Le Config Server est protégé par HTTP Basic Authentication. Les clients doivent s'authentifier pour accéder aux configurations :

```yaml
# Dans le microservice client (bootstrap.yml)
spring:
  cloud:
    config:
      uri: http://localhost:8888
      username: configadmin
      password: configadmin123
```

## 🔄 Rafraîchissement Dynamique

### Option 1 : Endpoint Actuator (par service)

```bash
curl -X POST -u configadmin:configadmin123 \
  http://localhost:8888/actuator/refresh
```

### Option 2 : Spring Cloud Bus (tous les services)

Avec Spring Cloud Bus + RabbitMQ/Kafka :

```bash
curl -X POST -u configadmin:configadmin123 \
  http://localhost:8888/actuator/bus-refresh
```

### Option 3 : Git Webhook

Configurer un webhook Git pour déclencher automatiquement le refresh lors d'un commit.

## 📊 Services Configurés

| Service | Port | Configuration |
|---------|------|---------------|
| Auth Service | 8081 | auth-service.yml |
| Patient Service | 8082 | patient-service.yml |
| Doctor Service | 8083 | doctor-service.yml |
| Appointment Service | 8084 | appointment-service.yml |
| Notification Service | 8085 | notification-service.yml |
| Payment Service | 8086 | payment-service.yml |
| File Service | 8087 | file-service.yml |
| API Gateway | 8080 | api-gateway.yml |

## 🔐 Encryption/Decryption

Pour chiffrer des valeurs sensibles (mots de passe, clés API) :

### 1. Générer une clé de chiffrement

```bash
keytool -genkeypair -alias config-server-key \
  -keyalg RSA -keysize 4096 \
  -keystore config-server.jks -storepass mypassword
```

### 2. Configurer le serveur

```yaml
encrypt:
  key-store:
    location: classpath:/config-server.jks
    password: mypassword
    alias: config-server-key
```

### 3. Chiffrer une valeur

```bash
curl -u configadmin:configadmin123 \
  http://localhost:8888/encrypt \
  -d "mysecretpassword"
```

### 4. Utiliser dans la configuration

```yaml
spring:
  datasource:
    password: '{cipher}AQA7h8F3k2...'
```

## 🧪 Tests

```bash
# Exécuter tous les tests
mvn test

# Test de connexion
curl -u configadmin:configadmin123 \
  http://localhost:8888/actuator/health
```

## 🐛 Dépannage

### Erreur d'authentification

Vérifier les credentials :
```bash
# Test avec curl
curl -v -u configadmin:configadmin123 \
  http://localhost:8888/auth-service/dev
```

### Configuration non trouvée

1. Vérifier le nom de l'application
2. Vérifier le profil
3. Vérifier les search-locations en mode native
4. Vérifier le repository Git en mode git

### Eureka non accessible

```bash
# Vérifier qu'Eureka est démarré
curl http://localhost:8761
```

## 📝 Best Practices

1. **Séparer les configurations sensibles** : Utiliser des variables d'environnement pour les secrets
2. **Utiliser Git en production** : Versioning et audit trail
3. **Chiffrer les secrets** : Utiliser l'encryption du Config Server
4. **Documenter les configurations** : Ajouter des commentaires
5. **Tester les changements** : Valider en dev/test avant prod
6. **Backup régulier** : Sauvegarder le repository de configuration

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📞 Support

Pour toute question ou problème :
- Email: support@smarthealthtunisia.com
- Documentation: https://docs.smarthealthtunisia.com
- Issues: GitHub Issues

---

**Made with ❤️ by Smart Health Tunisia Team**
