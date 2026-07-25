package com.medilinktunisia.authservice.exception;

import com.medilinktunisia.authservice.model.enums.UserStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Levée lorsqu'un compte valide (bon mot de passe) est bloqué à la connexion :
 * suspendu (avec éventuelle date de fin) ou désactivé.
 * Porte les informations nécessaires à l'affichage de la page dédiée côté front.
 */
@Getter
public class AccountStatusException extends RuntimeException {

    private final UserStatus status;
    private final String role;
    private final LocalDateTime suspendUntil;

    public AccountStatusException(UserStatus status, String role, LocalDateTime suspendUntil, String message) {
        super(message);
        this.status = status;
        this.role = role;
        this.suspendUntil = suspendUntil;
    }
}
