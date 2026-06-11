package com.medilinktunisia.pharmacyservice.model.dto;

import com.medilinktunisia.pharmacyservice.model.entity.Medication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationDto {
    private Long id;
    private String medicationCode;
    private String name;
    private String scientificName;
    private String manufacturer;
    private Medication.MedicationCategory category;
    private Medication.MedicationForm form;
    private String dosage;
    private String dosageUnit;
    private BigDecimal price;
    private BigDecimal subsidizedPrice;
    private Boolean reimbursable;
    private BigDecimal reimbursementRate;
    private Boolean requiresPrescription;
    private Medication.PrescriptionType prescriptionType;
    private String indications;
    private String contraindications;
    private String sideEffects;
    private String dosageInstructions;
    private String precautions;
    private String activeIngredient;
    private String composition;
    private String packaging;
    private Medication.MedicationStatus status;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
