package com.medilinktunisia.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDto {
    private Long id;
    private String name;
    private String dosage;
    private String forme;
    private String presentation;
    private BigDecimal price;
    private BigDecimal remboursement;
    private String dci;
    private String type;
    private Boolean prescriptionRequired;
    private Integer stockTotal;
    private Set<String> voieAdministration;
}
