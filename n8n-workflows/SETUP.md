# Configuration n8n + Telegram

## Architecture (3 workflows)

| # | Workflow | Déclencheur | Action |
|---|---|---|---|
| 1 | `workflow-1-notify-prescription.json` | Webhook (backend → n8n) | Envoie message Telegram avec boutons pharmacie |
| 2 | `workflow-2-handle-callback.json` | Telegram Trigger (callback_query) | Assigne la pharmacie choisie |
| 3 | `workflow-3-send-pickup-code.json` | Webhook (backend → n8n) | Génère code 6 chiffres → stocke → envoie au patient |

## Étapes

### 1. Installer n8n
```bash
npm install -g n8n
n8n start --tunnel
```
Accès : http://localhost:5678

### 2. Créer le bot Telegram
1. Telegram → rechercher **@BotFather**
2. `/newbot` → choisir nom et username
3. Copier le token

### 3. Configurer le credential Telegram dans n8n
- **Credentials** → **Add Credential** → **Telegram API**
- **Access Token** : coller le token du bot
- Nommer "MediLink Bot" (important : ce nom est utilisé dans les workflows)

### 4. Importer les workflows

Ouvrir chaque fichier dans n8n :
- **Workflows** → **Import from File**
- Sélectionner le fichier `.json`
- **Attention** : après import, vérifier que le credential Telegram est bien sélectionné (icône clé 🔑 sur le nœud Telegram)

### 5. Configurer le Webhook Telegram (Workflow 2)
Le **Telegram Trigger** (Workflow 2) doit être activé pour que Telegram envoie les callbacks à n8n.
1. Ouvrir Workflow 2
2. Cliquer sur **Telegram Trigger** → **Listen for events** (s'assurer que le bot est connecté)
3. **Activer** le workflow (toggle "Active")

### 6. Configurer les webhooks backend (Workflows 1 & 3)

Dans `application.yml` du prescription-service :
```yaml
n8n:
  webhook:
    base-url: http://localhost:5678/webhook
```

Le backend appelle automatiquement :
- `POST {base-url}/prescription-created` (Workflow 1)
- `POST {base-url}/prescription-prepared` (Workflow 3)

### 7. Lier un patient au bot Telegram
Le patient envoie `/start` au bot Telegram et son email.
Le bot appelle :
```
PUT http://localhost:8765/api/auth/patients/telegram
Body: { "email": "patient@email.com", "telegramChatId": "123456789" }
```

## Flux complet

```
1. Médecin crée ordonnance (status: SOUMISE)
   → Backend appelle webhook n8n /prescription-created
   → Workflow 1 envoie message Telegram avec boutons pharmacies

2. Patient clique sur une pharmacie
   → Workflow 2 reçoit le callback Telegram
   → Workflow 2 appelle PUT /prescriptions/{id}/assign-pharmacy
   → Workflow 2 répond au callback "Pharmacie sélectionnée!"

3. Pharmacien prépare l'ordonnance (status: PREPAREE)
   → Backend appelle webhook n8n /prescription-prepared
   → Workflow 3 génère code 6 chiffres
   → Workflow 3 stocke code via POST /prescriptions/{id}/pickup-code
   → Workflow 3 envoie code au patient Telegram

4. Patient présente le code en pharmacie
   → Pharmacien saisit le code dans l'app
   → Backend valide le code et passe en RETIREE
```

## URLs des webhooks n8n

| Webhook | Backend → n8n | Payload |
|---|---|---|
| `POST /webhook/prescription-created` | PrescriptionService (SOUMISE) | `{ id, baseUrl }` |
| `POST /webhook/prescription-prepared` | PrescriptionService (PREPAREE) | `{ id, baseUrl }` |

## Résolution des problèmes

- **"Patient has no linked Telegram chat"** : le patient doit d'abord envoyer `/start` au bot
- **Erreur Telegram "chat not found"** : le patient n'a pas initié la conversation
- **Workflow 2 ne reçoit pas les callbacks** : vérifier que le workflow est ACTIF et que le trigger Telegram est bien connecté
