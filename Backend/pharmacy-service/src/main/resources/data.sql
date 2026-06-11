-- Insertion de médicaments de base dans le référentiel (exemples)
-- Ces données seront chargées au démarrage de l'application

-- Médicaments sans ordonnance
INSERT INTO medications (medication_code, name, scientific_name, manufacturer, category, form, dosage, dosage_unit, price, requires_prescription, active, status, created_at, updated_at)
VALUES 
('MED001', 'Paracétamol 500mg', 'Paracetamol', 'PharmaCorp', 'ANALGESIC', 'TABLET', '500', 'mg', 2.500, false, true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED002', 'Ibuprofène 400mg', 'Ibuprofen', 'MediLab', 'ANALGESIC', 'TABLET', '400', 'mg', 3.200, false, true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED003', 'Aspirine 100mg', 'Acetylsalicylic Acid', 'PharmaCorp', 'ANALGESIC', 'TABLET', '100', 'mg', 1.800, false, true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED004', 'Vitamine C 1000mg', 'Ascorbic Acid', 'VitaHealth', 'VITAMIN_SUPPLEMENT', 'TABLET', '1000', 'mg', 5.500, false, true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED005', 'Sirop contre la toux', 'Dextromethorphan', 'CoughRelief', 'RESPIRATORY', 'SYRUP', '15', 'mg/5ml', 8.900, false, true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (medication_code) DO NOTHING;

-- Médicaments avec ordonnance
INSERT INTO medications (medication_code, name, scientific_name, manufacturer, category, form, dosage, dosage_unit, price, requires_prescription, prescription_type, active, status, created_at, updated_at)
VALUES 
('MED006', 'Amoxicilline 500mg', 'Amoxicillin', 'AntiBioTech', 'ANTIBIOTIC', 'CAPSULE', '500', 'mg', 12.500, true, 'STANDARD', true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED007', 'Azithromycine 250mg', 'Azithromycin', 'AntiBioTech', 'ANTIBIOTIC', 'TABLET', '250', 'mg', 15.800, true, 'STANDARD', true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED008', 'Oméprazole 20mg', 'Omeprazole', 'GastroMed', 'GASTROINTESTINAL', 'CAPSULE', '20', 'mg', 9.500, true, 'STANDARD', true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED009', 'Atorvastatine 20mg', 'Atorvastatin', 'CardioHealth', 'CARDIOVASCULAR', 'TABLET', '20', 'mg', 18.200, true, 'STANDARD', true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MED010', 'Metformine 850mg', 'Metformin', 'DiabetesCare', 'ENDOCRINE', 'TABLET', '850', 'mg', 6.700, true, 'STANDARD', true, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (medication_code) DO NOTHING;
