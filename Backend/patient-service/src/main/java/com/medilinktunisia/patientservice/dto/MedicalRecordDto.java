package com.medilinktunisia.patientservice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Dossier médical renvoyé au frontend pour l'affichage dans le panel patient.
 */
@Data
@Builder
public class MedicalRecordDto {
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
