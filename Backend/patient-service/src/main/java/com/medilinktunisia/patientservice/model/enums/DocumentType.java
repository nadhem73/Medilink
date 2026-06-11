package com.medilinktunisia.patientservice.model.enums;

public enum DocumentType {
    X_RAY("Radiographie"),
    MRI("IRM"),
    CT_SCAN("Scanner"),
    ULTRASOUND("Échographie"),
    MEDICAL_REPORT("Compte-rendu médical"),
    MEDICAL_CERTIFICATE("Certificat médical"),
    LAB_RESULT("Résultat d'analyse"),
    PRESCRIPTION("Ordonnance"),
    OTHER("Autre");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
