# Teleconsultation Service

Service de gestion des téléconsultations et consultations vidéo en temps réel pour Smart Health Tunisia.

## Description

Ce microservice gère l'ensemble des fonctionnalités de téléconsultation incluant :
- Sessions de consultations vidéo via WebRTC
- Chat en temps réel via WebSocket
- Partage de documents pendant les consultations
- Historique des consultations et messages

## Architecture Technique

### Technologies Utilisées

- **Spring Boot 3.2.0** - Framework principal
- **Spring WebSocket** - Communication bidirectionnelle temps réel
- **STOMP** - Protocole de messaging au-dessus de WebSocket
- **WebRTC** - Communication vidéo peer-to-peer
- **Spring Data JPA** - Persistence
- **PostgreSQL** - Base de données
- **SockJS** - Fallback pour les navigateurs sans WebSocket

### Composants WebSocket

#### 1. WebSocketConfig
Configuration principale des endpoints WebSocket :
- **Endpoint** : `/ws` (avec et sans SockJS)
- **Message Broker** : `/topic` (broadcast) et `/queue` (point-to-point)
- **Application Prefix** : `/app`
- **User Destination** : `/user`

#### 2. WebSocketEventListener
Gestion des événements de connexion/déconnexion :
- Détecte les connexions et déconnexions
- Notifie les autres participants
- Maintient le registre des sessions actives

#### 3. WebSocketSessionManager
Gestionnaire centralisé des sessions WebSocket :
- Tracking des participants par consultation
- Comptage des participants actifs
- Gestion du cycle de vie des sessions

#### 4. WebSocketAuthInterceptor
Intercepteur d'authentification :
- Extraction des headers d'authentification
- Validation des tokens JWT
- Association user/session

#### 5. WebRTCSignalingHandler
Gestionnaire des signaux WebRTC :
- Échange d'offres/réponses SDP
- Transmission des candidats ICE
- Contrôles média (mute/unmute)

## Fonctionnalités Principales

### 1. Gestion des Consultations

```
POST   /api/consultations                     - Créer une consultation
GET    /api/consultations/{id}                - Obtenir une consultation
GET    /api/consultations/doctor/{doctorId}   - Consultations d'un médecin
GET    /api/consultations/patient/{patientId} - Consultations d'un patient
PUT    /api/consultations/{id}/status         - Mettre à jour le statut
DELETE /api/consultations/{id}                - Supprimer une consultation
```

### 2. Communication WebSocket

#### Chat en Temps Réel

**Se connecter :**
```javascript
const socket = new SockJS('http://localhost:8087/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
    username: 'Dr. Ahmed',
    userId: '123',
    consultationId: '456'
}, onConnected, onError);
```

**Envoyer un message :**
```javascript
// Subscribe to receive messages
stompClient.subscribe('/topic/consultation/456', onMessageReceived);

// Send a message
stompClient.send('/app/consultation/456/message', {}, JSON.stringify({
    type: 'CHAT',
    sender: 'Dr. Ahmed',
    senderId: 123,
    content: 'Bonjour, comment allez-vous?',
    timestamp: new Date()
}));
```

#### Communication Vidéo WebRTC

**Envoyer une offre :**
```javascript
stompClient.send('/app/consultation/456/webrtc/offer', {}, JSON.stringify({
    fromUserId: 123,
    toUserId: 789,
    sdp: offer.sdp,
    type: 'offer'
}));
```

**Envoyer une réponse :**
```javascript
stompClient.send('/app/consultation/456/webrtc/answer', {}, JSON.stringify({
    fromUserId: 789,
    toUserId: 123,
    sdp: answer.sdp,
    type: 'answer'
}));
```

**Envoyer un candidat ICE :**
```javascript
stompClient.send('/app/consultation/456/webrtc/candidate', {}, JSON.stringify({
    fromUserId: 123,
    toUserId: 789,
    candidate: candidate.candidate,
    sdpMid: candidate.sdpMid,
    sdpMLineIndex: candidate.sdpMLineIndex
}));
```

**Contrôles média :**
```javascript
// Mute audio
stompClient.send('/app/consultation/456/control', {}, JSON.stringify({
    fromUserId: 123,
    type: 'audio-mute',
    data: { muted: true }
}));

// Disable video
stompClient.send('/app/consultation/456/control', {}, JSON.stringify({
    fromUserId: 123,
    type: 'video-disable',
    data: { disabled: true }
}));
```

### 3. Partage de Documents

```
POST   /api/consultations/{id}/documents       - Partager un document
GET    /api/consultations/{id}/documents       - Liste des documents partagés
GET    /api/consultations/documents/{docId}    - Télécharger un document
DELETE /api/consultations/documents/{docId}    - Supprimer un document
```

### 4. Historique des Messages

```
GET    /api/consultations/{id}/messages        - Historique des messages
POST   /api/consultations/{id}/messages        - Envoyer un message (REST fallback)
```

## Modèle de Données

### Teleconsultation
```java
{
    "id": 1,
    "consultationNumber": "TC20240115001",
    "doctorId": 123,
    "doctorName": "Dr. Ahmed Ben Ali",
    "patientId": 456,
    "patientName": "Mohamed Trabelsi",
    "scheduledStartTime": "2024-01-15T10:00:00",
    "scheduledEndTime": "2024-01-15T10:30:00",
    "actualStartTime": "2024-01-15T10:02:00",
    "actualEndTime": "2024-01-15T10:28:00",
    "status": "COMPLETED",
    "notes": "Consultation de suivi",
    "diagnosis": "État stable",
    "createdAt": "2024-01-10T14:30:00"
}
```

### ConsultationMessage
```java
{
    "id": 1,
    "consultationId": 1,
    "senderId": 123,
    "senderName": "Dr. Ahmed",
    "senderRole": "DOCTOR",
    "content": "Bonjour, comment vous sentez-vous?",
    "messageType": "TEXT",
    "timestamp": "2024-01-15T10:05:00"
}
```

### SharedDocument
```java
{
    "id": 1,
    "consultationId": 1,
    "fileName": "radiographie.pdf",
    "fileSize": 2048576,
    "fileType": "application/pdf",
    "uploadedBy": 123,
    "uploaderName": "Dr. Ahmed",
    "uploadedAt": "2024-01-15T10:10:00"
}
```

## Statuts de Consultation

| Status | Description |
|--------|-------------|
| **SCHEDULED** | Consultation planifiée, pas encore commencée |
| **IN_PROGRESS** | Consultation en cours |
| **COMPLETED** | Consultation terminée avec succès |
| **CANCELLED** | Consultation annulée |
| **NO_SHOW** | Patient absent |

## Types de Messages

| Type | Description |
|------|-------------|
| **CHAT** | Message de chat texte |
| **JOIN** | Notification de connexion d'un participant |
| **LEAVE** | Notification de déconnexion d'un participant |
| **SYSTEM** | Message système automatique |

## Sécurité

### Authentification WebSocket

Lors de la connexion WebSocket, les headers suivants sont requis :
```javascript
{
    username: 'nom_utilisateur',
    userId: 'ID_utilisateur',
    consultationId: 'ID_consultation'
}
```

### Autorisation

- Seuls les participants autorisés (médecin et patient de la consultation) peuvent se connecter
- Les messages sont diffusés uniquement aux participants de la consultation
- Le partage de documents est limité aux participants

## Configuration

### Variables d'Environnement

```properties
# Service
SERVER_PORT=8087
SPRING_APPLICATION_NAME=teleconsultation-service

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/teleconsultation_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# WebSocket
SPRING_WEBSOCKET_ALLOWED_ORIGINS=*

# File Upload
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=10MB

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
```

## Flow de Communication WebRTC

### 1. Établissement de la Connexion

```
Patient                          Server                          Doctor
  |                                 |                               |
  |--- CONNECT (WebSocket) -------->|                               |
  |<-- CONNECTED -------------------|                               |
  |                                 |<--- CONNECT (WebSocket) ------|
  |                                 |---- CONNECTED --------------->|
  |                                 |                               |
  |--- Subscribe: /topic/consultation/456 ----------------------->|
  |<-- JOIN notification -----------------------------------------|
```

### 2. Négociation WebRTC

```
Patient                                                          Doctor
  |                                                                 |
  |--- Create Offer (SDP) ----------------------------------------->|
  |    /app/consultation/456/webrtc/offer                          |
  |                                                                 |
  |<-- Create Answer (SDP) ----------------------------------------|
  |    /app/consultation/456/webrtc/answer                         |
  |                                                                 |
  |<--> Exchange ICE Candidates ----------------------------------->|
  |    /app/consultation/456/webrtc/candidate                      |
  |                                                                 |
  |<--> Direct Media Stream (P2P) -------------------------------->|
```

### 3. Contrôles et Chat

```
Patient                          Server                          Doctor
  |                                 |                               |
  |--- Chat Message --------------->|                               |
  |                                 |--- Broadcast ---------------->|
  |                                 |                               |
  |                                 |<-- Mute Audio ----------------|
  |<-- Broadcast --------------------|                              |
```

## Gestion des Participants

Le `WebSocketSessionManager` maintient la liste des participants actifs :

```java
// Obtenir les participants actifs
List<SessionInfo> participants = sessionManager.getConsultationSessions(consultationId);

// Vérifier si un utilisateur est connecté
boolean isConnected = sessionManager.isUserConnected(consultationId, userId);

// Obtenir le nombre de participants
int count = sessionManager.getActiveParticipantsCount(consultationId);
```

## Monitoring et Métriques

### Endpoints Actuator

```
GET /actuator/health                    - État de santé
GET /actuator/metrics                   - Métriques
GET /actuator/websocket/sessions        - Sessions WebSocket actives
```

### Métriques Personnalisées

- Nombre de consultations actives
- Nombre de participants connectés
- Durée moyenne des consultations
- Messages par consultation

## Gestion des Erreurs

### Codes d'Erreur WebSocket

| Code | Description |
|------|-------------|
| 1000 | Fermeture normale |
| 1001 | Endpoint parti |
| 1002 | Erreur de protocole |
| 1003 | Type de données non supporté |
| 1006 | Connexion anormale |
| 1011 | Erreur serveur |

### Reconnexion Automatique

Le client doit implémenter une logique de reconnexion :

```javascript
function connect() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect(headers, 
        () => console.log('Connected'),
        (error) => {
            console.error('Connection error:', error);
            // Retry after 5 seconds
            setTimeout(connect, 5000);
        }
    );
}
```

## Limitations et Considérations

### Scalabilité

- Le broker de messages en mémoire est limité à une instance unique
- Pour la production, utiliser un message broker externe (RabbitMQ, Redis)
- Les sessions WebSocket ne sont pas partagées entre instances

### Performance

- Maximum de 2 participants par consultation recommandé (1-to-1)
- Durée maximale d'une consultation : 1 heure
- Taille maximale des fichiers partagés : 10 MB

### Réseau

- WebRTC nécessite une connexion stable
- Utiliser un serveur STUN/TURN pour traverser les NAT/Firewalls
- Bande passante minimale recommandée : 1 Mbps

## Exemples d'Intégration

### Client React

```jsx
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const TeleconsultationRoom = ({ consultationId, userId, username }) => {
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8087/ws');
        const client = Stomp.over(socket);

        client.connect({
            username,
            userId: userId.toString(),
            consultationId: consultationId.toString()
        }, () => {
            // Subscribe to messages
            client.subscribe(`/topic/consultation/${consultationId}`, (message) => {
                const chatMessage = JSON.parse(message.body);
                setMessages(prev => [...prev, chatMessage]);
            });

            setStompClient(client);
        });

        return () => client?.disconnect();
    }, [consultationId, userId, username]);

    const sendMessage = (content) => {
        if (stompClient && stompClient.connected) {
            stompClient.send(`/app/consultation/${consultationId}/message`, {}, 
                JSON.stringify({
                    type: 'CHAT',
                    sender: username,
                    senderId: userId,
                    content,
                    timestamp: new Date()
                })
            );
        }
    };

    return (
        <div>
            {/* UI components */}
        </div>
    );
};
```

## Tests

```bash
# Tests unitaires
./mvnw test

# Tests d'intégration
./mvnw verify

# Test WebSocket avec curl
wscat -c ws://localhost:8087/ws
```

## Déploiement

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Docker
docker build -t teleconsultation-service .
docker run -p 8087:8087 teleconsultation-service
```

## Améliorations Futures

- [ ] Support multi-participants (webinaires)
- [ ] Enregistrement des consultations
- [ ] Transcription automatique
- [ ] Traduction en temps réel
- [ ] Réactions et emojis
- [ ] Partage d'écran
- [ ] Whiteboard collaboratif
- [ ] Intégration avec services de TURN/STUN cloud
- [ ] Qualité de service adaptative
- [ ] Chiffrement end-to-end

## Ressources

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [STOMP Protocol](https://stomp.github.io/)
- [WebRTC Documentation](https://webrtc.org/)
- [SockJS Protocol](https://github.com/sockjs/sockjs-protocol)

## Support

Pour toute question ou problème, consultez la documentation complète ou contactez l'équipe de développement.
