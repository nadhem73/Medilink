-- MediLink Tunisia — Database Initialization Script
-- Creates all databases required by the microservices.

SELECT 'CREATE DATABASE medilink_auth'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_auth')\gexec

SELECT 'CREATE DATABASE medilink_patients'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_patients')\gexec

SELECT 'CREATE DATABASE medilink_doctors'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_doctors')\gexec

SELECT 'CREATE DATABASE medilink_pharmacy'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_pharmacy')\gexec

SELECT 'CREATE DATABASE medilink_prescriptions'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_prescriptions')\gexec

SELECT 'CREATE DATABASE medilink_bilan'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_bilan')\gexec

SELECT 'CREATE DATABASE medilink_monitoring'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_monitoring')\gexec

SELECT 'CREATE DATABASE medilink_ai'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'medilink_ai')\gexec