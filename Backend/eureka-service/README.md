# Eureka Service (Service Discovery)

## 📋 Description

Eureka Service est le serveur de découverte de services qui permet l'enregistrement et la découverte de tous les microservices de la plateforme Smart Health Tunisia.

## 🎯 Rôle

- **Enregistrement des services** : Tous les microservices s'enregistrent auprès d'Eureka
- **Découverte de services** : Permet aux services de se découvrir mutuellement
- **Health Monitoring** : Surveillance de la santé de tous les services
- **Load Balancing** : Facilite la répartition de charge entre les instances

## 🚀 Démarrage

### Prérequis
- Java 21
- Maven 3.9+

### Commandes

```bash
# Développement
mvn spring-boot:run

# Production
mvn clean package
java -jar target/eureka-service-1.0.0.jar --spring.profiles.active=prod
```

## 🔧 Configuration

### Port
- **Développement** : 8761
- **Production** : 8761

### Accès au Dashboard

```
URL: http://localhost:8761
Username: admin
Password: admin123 (dev) / changeme (prod)
```

## 📊 Endpoints

### Dashboard Eureka
```
GET http://localhost:8761
```

### Health Check
```
GET http://localhost:8761/actuator/health
```

### Eureka API
```
GET http://localhost:8761/eureka/apps
```

## 🔐 Sécurité

- Authentification HTTP Basic pour le dashboard
- Les endpoints Eureka sont publics pour permettre l'enregistrement des services
- Credentials configurables par variables d'environnement en production

## 🌍 Profils

### dev
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- Self-preservation désactivé
- Logs en mode DEBUG
- Credentials: admin/admin123

### prod
```bash
java -jar eureka-service.jar --spring.profiles.active=prod
```
- Self-preservation activé
- Logs en mode INFO/WARN
- Credentials depuis variables d'environnement

## 📦 Services Enregistrés

Les services suivants s'enregistrent automatiquement :

1. **auth-service** (Port 8081)
2. **patient-service** (Port 8082)
3. **doctor-service** (Port 8083)
4. **appointment-service** (Port 8084)
5. **prescription-service** (Port 8085)
6. **pharmacy-service** (Port 8086)
7. **laboratory-service** (Port 8087)
8. **ambulance-service** (Port 8088)
9. **notification-service** (Port 8089)
10. **payment-service** (Port 8090)
11. **teleconsultation-service** (Port 8091)
12. **ai-service** (Port 8092)
13. **file-service** (Port 8093)
14. **admin-service** (Port 8094)
15. **geolocation-service** (Port 8095)
16. **analytics-service** (Port 8096)
17. **api-gateway** (Port 8080)

## 🛠️ Maintenance

### Vérifier les services enregistrés
```bash
curl http://admin:admin123@localhost:8761/eureka/apps
```

### Logs
Les logs sont stockés dans `logs/eureka-service.log`

## 📝 Variables d'Environnement (Production)

```bash
EUREKA_USERNAME=admin
EUREKA_PASSWORD=secure_password_here
EUREKA_HOSTNAME=eureka-service.domain.com
```

## 🔄 Self-Preservation Mode

En production, Eureka active le mode "Self-Preservation" qui :
- Protège contre les pannes réseau temporaires
- Évite l'éviction massive de services
- Maintient le registre en cas de problème de connectivité

## 📈 Métriques

Accès aux métriques via Actuator :
```
GET http://localhost:8761/actuator/metrics
GET http://localhost:8761/actuator/prometheus
```

## 🐛 Troubleshooting

### Service ne s'enregistre pas
1. Vérifier que Eureka Service est démarré
2. Vérifier l'URL de connexion dans le service client
3. Vérifier les logs du service client

### Dashboard inaccessible
1. Vérifier les credentials
2. Vérifier que le port 8761 n'est pas occupé
3. Consulter les logs

## 👥 Équipe

Développé par l'équipe Smart Health Tunisia

## 📄 Licence

Propriétaire - Smart Health Tunisia © 2025
