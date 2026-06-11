-- ============================================
-- MIGRATION: is_active (boolean) → status (enum)
-- Date: 8 juin 2026
-- ============================================

-- ÉTAPE 1: Supprimer la colonne is_active
ALTER TABLE users DROP COLUMN IF EXISTS is_active CASCADE;

-- ÉTAPE 2: Ajouter la colonne status avec enum
-- Note: Hibernate créera automatiquement la contrainte CHECK lors du prochain démarrage

-- ÉTAPE 3: Vérifier la structure
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

-- RÉSULTAT ATTENDU:
-- Les colonnes doivent inclure:
-- - status (varchar(20), NOT NULL)
-- - gender (varchar(10), NOT NULL)
-- - address, birth_date, cin, etc.

-- ============================================
-- COMMANDE D'EXÉCUTION:
-- psql -U postgres -d medilink_auth -f MIGRATION_STATUS_TO_ENUM.sql
-- ============================================
