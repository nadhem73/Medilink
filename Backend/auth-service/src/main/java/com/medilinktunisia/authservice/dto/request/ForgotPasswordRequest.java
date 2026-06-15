package com.medilinktunisia.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Demande de réinitialisation de mot de passe.
 * Selon le rôle, l'identifiant fourni diffère :
 * <ul>
 *   <li>patient : son email d'inscription ({@code email}) ;</li>
 *   <li>médecin : son numéro d'ordre ({@code licenseNumber}).</li>
 * </ul>
 */
@Data
public class ForgotPasswordRequest {

    /** patient | doctor (la pharmacie est ignorée pour le moment). */
    @NotBlank
    private String role;

    /** Identifiant patient : email du compte. */
    private String email;

    /** Identifiant médecin : numéro d'ordre. */
    private String licenseNumber;
}
