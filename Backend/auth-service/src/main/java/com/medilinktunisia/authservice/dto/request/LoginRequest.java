package com.medilinktunisia.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Requête de connexion.
 * Le patient se connecte par {@code cin} ; les autres rôles par {@code email}.
 * L'un des deux identifiants doit être fourni (vérifié dans AuthService).
 */
@Data
public class LoginRequest {

    /** Identifiant pharmacie (et fallback). */
    private String email;

    /** Identifiant patient : numéro de carte d'identité nationale. */
    private String cin;

    /** Identifiant médecin : numéro d'ordre. */
    private String licenseNumber;

    @NotBlank
    private String password;
}
