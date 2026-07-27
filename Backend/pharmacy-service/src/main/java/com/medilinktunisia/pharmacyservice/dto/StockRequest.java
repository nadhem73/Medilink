package com.medilinktunisia.pharmacyservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StockRequest {
    private Long medicamentId;
    private String numeroLot;
    private Integer quantiteEnStock;
    private LocalDate dateFabrication;
    private LocalDate dateExpiration;
    private String emplacement;
}
