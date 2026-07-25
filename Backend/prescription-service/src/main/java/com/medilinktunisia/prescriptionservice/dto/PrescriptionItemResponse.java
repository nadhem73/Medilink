package com.medilinktunisia.prescriptionservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrescriptionItemResponse {
    private Long id;
    private Long medicamentId;
    private String medicamentName;
    private String dosage;
    private String forme;
    private String posologie;
    private Integer dureeTraitement;
    private String voieAdministration;
    private String instructions;
    private Integer quantitePrescrite;
}
