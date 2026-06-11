package com.medilinktunisia.pharmacyservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispensationItemDto {
    private Long id;
    private Long medicationId;
    private String medicationName;
    private String medicationCode;
    private Integer quantityPrescribed;
    private Integer quantityDispensed;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal reimbursementAmount;
    private String dosageInstructions;
    private String pharmacistInstructions;
    private Boolean substituted;
    private Long originalMedicationId;
    private String originalMedicationName;
    private String substitutionReason;
}
