package com.medilinktunisia.pharmacyservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MedicamentRequest {
    private String name;
    private String dosage;
    private String forme;
    private String presentation;
    private BigDecimal price;
    private BigDecimal remboursement;
    private String dci;
    private String type;
    private Boolean prescriptionRequired;
    private String imageUrl;
}
