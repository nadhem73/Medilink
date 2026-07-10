package com.medilinktunisia.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Corps renvoyé (HTTP 403) quand la connexion est refusée pour cause de
 * compte suspendu ou désactivé. Permet au front d'afficher la page adaptée.
 */
@Data
@Builder
public class AccountStatusResponse {
    private String message;
    private boolean success;
    private String status;         // SUSPENDED | INACTIVE
    private String role;           // PATIENT | DOCTOR | PHARMACY | ...
    private LocalDateTime suspendUntil;
}
