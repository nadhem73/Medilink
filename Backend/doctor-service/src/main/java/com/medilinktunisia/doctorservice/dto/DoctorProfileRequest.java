package com.medilinktunisia.doctorservice.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Données du profil opérationnel reçues à la création du compte médecin,
 * ou envoyées par le panel médecin lors d'une mise à jour.
 */
@Data
public class DoctorProfileRequest {
    private Long userId;
    private Boolean available;
    private String biography;
    private BigDecimal fee;
}
