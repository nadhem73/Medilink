package com.medilinktunisia.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * DTO renvoyé au frontend pour afficher la liste des pharmacies
 * (ex. recherche de pharmacie pour une ordonnance).
 */
@Data
@Builder
public class PharmacyListDto {
    private Long id;
    private String pharmacyName;
    private String email;
    private String phone;
    private String address;
    private String licenseNumber;
    private String openingHours;
    private Double latitude;
    private Double longitude;
}
