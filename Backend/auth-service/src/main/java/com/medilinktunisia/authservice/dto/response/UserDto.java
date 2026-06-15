package com.medilinktunisia.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Représentation publique de l'utilisateur renvoyée au frontend.
 * Les champs correspondent à l'interface AuthResponse.user du frontend Angular.
 */
@Data
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String gender;
    private String status;
    private boolean isEmailVerified;
    private List<String> roles;
    private LocalDateTime createdAt;
}
