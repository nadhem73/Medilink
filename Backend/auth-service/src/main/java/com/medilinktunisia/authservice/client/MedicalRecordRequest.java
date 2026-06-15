package com.medilinktunisia.authservice.client;

import lombok.Builder;
import lombok.Data;

/**
 * Données médicales transmises au patient-service lors de l'inscription
 * pour créer le dossier médical (MedicalRecord) du patient.
 */
@Data
@Builder
public class MedicalRecordRequest {
    private Long userId;
    private String bloodGroup;
    private Double height;
    private Double weight;
    private String allergies;
    private String chronicDiseases;
    private String currentTreatments;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String insuranceCompany;
    private String insuranceNumber;
}
