package com.medilinktunisia.doctorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Profil opérationnel du médecin renvoyé au frontend pour le panel médecin.
 */
@Data
@Builder
public class DoctorProfileDto {
    private Long userId;
    private Boolean available;
    private String biography;
    private BigDecimal fee;
    private String debutMatin;
    private String finMatin;
    private String debutApresMidi;
    private String finApresMidi;
}
