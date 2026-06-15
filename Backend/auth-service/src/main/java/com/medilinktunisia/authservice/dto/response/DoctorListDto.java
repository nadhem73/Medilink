package com.medilinktunisia.authservice.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * DTO renvoyé au frontend pour afficher la liste des médecins
 * dans l'écran de prise de rendez-vous du patient.
 */
@Data
@Builder
public class DoctorListDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialty;
    private String hospital;
    private String licenseNumber;
}
