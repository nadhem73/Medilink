package com.medilinktunisia.pharmacyservice.dto;

import lombok.Data;

/**
 * Données du profil opérationnel reçues à la création du compte pharmacie,
 * ou envoyées par le panel pharmacie lors d'une mise à jour.
 */
@Data
public class PharmacyProfileRequest {
    private Long userId;
    private Boolean open;
    private Boolean nightDuty;
    private Integer stockAlertThreshold;
    private String debutMatin;
    private String finMatin;
    private String debutApresMidi;
    private String finApresMidi;
}
