package com.medilinktunisia.pharmacyservice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Profil opérationnel de la pharmacie renvoyé au frontend pour le panel pharmacie.
 */
@Data
@Builder
public class PharmacyProfileDto {
    private Long userId;
    private Boolean open;
    private Boolean nightDuty;
    private Integer stockAlertThreshold;
    private String debutMatin;
    private String finMatin;
    private String debutApresMidi;
    private String finApresMidi;
}
