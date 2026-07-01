package com.medilinktunisia.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationStockDto {
    private Long id;
    private Long medicamentId;
    private String medicamentName;
    private Integer quantiteEnStock;
    private LocalDate dateFabrication;
    private LocalDate dateExpiration;
    private String emplacement;
}
