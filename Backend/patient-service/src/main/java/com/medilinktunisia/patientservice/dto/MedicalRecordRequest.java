package com.medilinktunisia.patientservice.dto;

import lombok.Data;

/**
 * Données médicales reçues de l'auth-service à l'inscription d'un patient.
 */
@Data
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
