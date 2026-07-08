package com.medilinktunisia.prescriptionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemRequest {

    @NotNull
    private Long medicamentId;

    @NotBlank
    private String medicamentName;

    private String dosage;

    private String forme;

    @NotBlank
    private String posologie;

    private Integer dureeTraitement;

    private String voieAdministration;

    private String instructions;
}
