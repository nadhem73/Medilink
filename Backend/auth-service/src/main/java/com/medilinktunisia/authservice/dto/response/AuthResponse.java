package com.medilinktunisia.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Réponse d'authentification (login) contenant les tokens JWT et l'utilisateur.
 */
@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserDto user;
}
