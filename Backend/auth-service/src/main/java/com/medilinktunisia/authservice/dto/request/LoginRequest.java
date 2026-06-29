package com.medilinktunisia.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Requête de connexion.
 * Le patient se connecte par {@code cin}, le médecin et la pharmacie par
 * {@code licenseNumber} ; {@code email} sert de fallback.
 * L'un des identifiants doit être fourni (vérifié dans AuthService).
 */
@Data
public class LoginRequest {

    /** Identifiant fallback (email du compte). */
    private String email;

    /** Identifiant patient : numéro de carte d'identité nationale. */
    private String cin;

    /** Identifiant médecin (numéro d'ordre) ou pharmacie (numéro de licence). */
    private String licenseNumber;

    @NotBlank
    private String password;
}
