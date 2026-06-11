# 🚀 GUIDE EXÉCUTION MIGRATION isActive → status

## ⚠️ IMPORTANT
**Tous les fichiers Java sont corrects. Il reste uniquement à exécuter le script SQL dans PostgreSQL.**

---

## 📋 OPTION 1: Via pgAdmin (Recommandé - Interface graphique)

### Étapes:
1. **Ouvrir pgAdmin 4**
2. **Se connecter au serveur PostgreSQL**
   - Serveur: `localhost`
   - Port: `5432`
   - Utilisateur: `postgres`

3. **Sélectionner la base de données**
   - Naviguer vers: `Servers` → `PostgreSQL` → `Databases` → `medilink_auth`

4. **Ouvrir Query Tool**
   - Clic droit sur `medilink_auth` → `Query Tool`

5. **Copier-coller le script SQL**
   - Ouvrir le fichier: `Backend/auth-service/MIGRATION_STATUS_TO_ENUM.sql`
   - Copier tout le contenu
   - Coller dans Query Tool

6. **Exécuter le script**
   - Cliquer sur le bouton ▶️ (Execute/Refresh) ou `F5`

7. **Vérifier les résultats**
   - Message attendu: `Query returned successfully`
   - Vérifier dans l'onglet "Messages"

---

## 📋 OPTION 2: Via psql (Ligne de commande)

### Méthode A: Exécuter le fichier SQL directement
```bash
# Se placer dans le dossier du service
cd "C:\Users\asus\Desktop\MediLink Tunisia\Backend\auth-service"

# Exécuter le script
psql -U postgres -d medilink_auth -f MIGRATION_STATUS_TO_ENUM.sql
```

### Méthode B: Se connecter puis exécuter
```bash
# Se connecter à la base
psql -U postgres -d medilink_auth

# Dans psql, exécuter le script
\i MIGRATION_STATUS_TO_ENUM.sql

# Ou copier-coller le contenu directement
```

---

## ✅ VÉRIFICATION POST-MIGRATION

### 1. Vérifier les colonnes
```sql
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'users' 
  AND column_name IN ('status', 'is_active')
ORDER BY column_name;
```

**Résultat attendu:**
```
column_name | data_type        | is_nullable | column_default
------------+------------------+-------------+---------------
status      | character varying| NO          | NULL
```

**Note:** `is_active` ne doit PAS apparaître (supprimée).

### 2. Vérifier les données
```sql
SELECT id, email, status, first_name, last_name
FROM users
LIMIT 10;
```

**Résultat attendu:**
```
id | email              | status | first_name | last_name
---+--------------------+--------+------------+-----------
1  | admin@example.com  | ACTIVE | Admin      | User
```

### 3. Vérifier l'index
```sql
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'users' AND indexname = 'idx_status';
```

**Résultat attendu:**
```
indexname  | indexdef
-----------+----------------------------------------------------------
idx_status | CREATE INDEX idx_status ON public.users USING btree (status)
```

---

## 🔥 EN CAS D'ERREUR

### Erreur: "relation users does not exist"
**Cause:** La table `users` n'existe pas encore.

**Solution:**
```bash
# Démarrer le service une première fois pour créer les tables
cd "C:\Users\asus\Desktop\MediLink Tunisia\Backend\auth-service"
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Ctrl+C après quelques secondes (une fois les tables créées)
# Puis exécuter le script SQL
```

### Erreur: "column is_active does not exist"
**Cause:** Hibernate a déjà créé la table avec `status` (pas `is_active`).

**Solution:** La migration est déjà faite ! Vérifiez avec :
```sql
\d users  -- Dans psql
-- ou
SELECT * FROM information_schema.columns WHERE table_name = 'users';
```

### Erreur: "column status already exists"
**Cause:** Le script a déjà été exécuté partiellement.

**Solution:**
```sql
-- Vérifier l'état actuel
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'users' AND column_name IN ('status', 'is_active');

-- Si status existe et is_active aussi:
ALTER TABLE users DROP COLUMN IF EXISTS is_active CASCADE;

-- Si status n'existe pas:
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
```

---

## 🚀 APRÈS LA MIGRATION

### 1. Compiler le projet
```bash
cd "C:\Users\asus\Desktop\MediLink Tunisia\Backend\auth-service"
mvn clean compile
```

**Résultat attendu:** `BUILD SUCCESS`

### 2. Démarrer le service
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Résultat attendu:**
```
2026-06-08 10:00:00 - Started AuthServiceApplication in 5.123 seconds
2026-06-08 10:00:00 - Eureka client registered with Eureka server
```

### 3. Tester l'API
```bash
# Test de health check
curl http://localhost:8081/actuator/health

# Test d'inscription
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@medilink.tn",
    "password": "Test123!",
    "firstName": "Ahmed",
    "lastName": "Ben Ali",
    "phone": "+216 20 123 456",
    "birthDate": "1990-01-01",
    "gender": "MALE",
    "cin": "12345678",
    "role": "PATIENT"
  }'
```

**Résultat attendu:**
```json
{
  "message": "Inscription réussie! Veuillez vérifier votre email.",
  "success": true
}
```

---

## 📊 CHECKLIST COMPLÈTE

- [x] ✅ Tous les fichiers Java corrigés
- [ ] ⏳ Script SQL exécuté dans PostgreSQL
- [ ] ⏳ Colonnes vérifiées (status existe, is_active supprimée)
- [ ] ⏳ Service compilé avec succès
- [ ] ⏳ Service démarré sans erreur
- [ ] ⏳ API testée (inscription fonctionnelle)

---

## 🆘 BESOIN D'AIDE?

### Vérifier si PostgreSQL est démarré
```bash
# Windows - Services
services.msc

# Ou vérifier le port
netstat -an | findstr 5432
```

### Vérifier les logs du service
```bash
# Logs en temps réel
tail -f Backend/auth-service/logs/auth-service.log

# Dernières 100 lignes
tail -n 100 Backend/auth-service/logs/auth-service.log
```

### Réinitialiser complètement la base (ATTENTION: Perte de données)
```sql
-- Se connecter à postgres
psql -U postgres

-- Supprimer et recréer la base
DROP DATABASE IF EXISTS medilink_auth;
CREATE DATABASE medilink_auth;

-- Se déconnecter
\q

-- Relancer le service (Hibernate créera les tables)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 🎯 PROCHAINE ÉTAPE

Une fois le service auth-service fonctionnel:
1. Configurer CORS dans API Gateway
2. Configurer SecurityConfig pour routes publiques
3. Tester l'inscription depuis le frontend Angular (localhost:4200)
