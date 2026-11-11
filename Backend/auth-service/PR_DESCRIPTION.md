## Description

Refonte complète de la gestion de profil utilisateur pour les 4 panels (Patient, Médecin, Pharmacie, Administrateur) avec ajout de la liaison Telegram pour les notifications n8n.

## Changements

### Frontend — Pages Settings (4 panels)

- **Nouveau design** : Header de profil avec avatar/initiales, carte d'identité (nom, email, badge rôle), grille 2 colonnes pour les sections
- **Sections** : Informations personnelles, Sécurité (mot de passe modifiable in-place), Notifications (toggles email/SMS/push), Préférences, Zone dangereuse (suppression)
- **Mode édition inline** : Champs éditables avec sauvegarde directe (PUT /api/auth/profile)
- **Changement de mot de passe** : Formulaire avec validation (nouveau mot de passe + confirmation)
- **Couleurs par panel** :
  - Patient : Turquoise `#1f7a8c`
  - Médecin : Vert oasis `#2e8b57`
  - Pharmacie : Violet `#6d28d9`
  - Admin : Or `#c9962f` / Anthracite `#1a1a2e`

### Frontend — Liaison Telegram

- **QR code** généré côté serveur (deep link vers `@medilink_tunisie_bot`)
- **Saisie manuelle** : champ texte + bouton Lier (fallback)
- **Auto-link** : Flow en 2 étapes — (1) clique Vérifier → supprime webhook, (2) envoie message au bot → clique Continuer → lie automatiquement
- Instructions claires avec étapes numérotées
- Messages de succès/erreur avec timeout auto-effaçant

### Backend — auth-service

- **Nouveau service** : `TelegramService.java` — gestion webhook Telegram (get/delete/set), polling `getUpdates`
- **2 endpoints** :
  - `POST /patients/telegram/auto-link` — Supprime webhook, tente getUpdates
  - `POST /patients/telegram/complete-linking` — Vérifie les messages, lie le chatId, restaure le webhook
- **Synchronisation** `synchronized` pour éviter les conflits 409 entre appels concurrents
- **DTOs** : `AutoLinkTelegramRequest`, `LinkTelegramRequest`

### Backend — api-gateway

- **Timeout augmenté** à 120s pour `authServiceCircuitBreaker` dans resilience4j timelimiter (nécessaire pour le long polling Telegram)

### Fix

- **Sidebar admin** : Sélecteur CSS corrigé (`.sidebar .admin` → `.sidebar.admin`) pour appliquer la bonne couleur or

## Fichiers modifiés

| Fichier | Description |
|---------|-------------|
| `Frontend/.../settings.component.html` | Patient — Template settings complet |
| `Frontend/.../settings.component.ts` | Patient — Logique complète (édition, password, Telegram) |
| `Frontend/.../settings.component.scss` | Patient — Styles (640+ lignes, responsive) |
| `Frontend/.../settings.component.html/ts/scss` | Médecin, Pharmacie, Admin — Copie adaptée (couleurs) |
| `Frontend/.../sidebar.component.scss` | Admin sidebar — Fix sélecteur CSS |
| `Frontend/.../auth.service.ts` | Méthodes HTTP Telegram ajoutées |
| `Backend/auth-service/.../TelegramService.java` | Nouveau service de liaison Telegram |
| `Backend/auth-service/.../AuthController.java` | 2 endpoints Telegram + auto-link |
| `Backend/auth-service/.../AutoLinkTelegramRequest.java` | DTO requête |
| `Backend/api-gateway/.../application.yml` | Timeout auth-service augmenté |

## Tests

- [ ] Vérifier les 4 panels settings (couleurs, mise en page)
- [ ] Tester édition profil (champs modifiables → sauvegarde)
- [ ] Tester changement mot de passe
- [ ] Tester liaison Telegram manuelle (entrer chatId → Lier)
- [ ] Tesser auto-link : cliquer Vérifier → envoyer message au bot → cliquer Continuer
- [ ] Vérifier que le webhook n8n est restauré après la liaison
