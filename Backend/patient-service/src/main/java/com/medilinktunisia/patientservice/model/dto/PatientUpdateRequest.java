package com.medilinktunisia.patientservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientUpdateRequest {

    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String firstName;

    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{8,20}$", message = "Numéro de téléphone invalide")
    private String phone;

    @Email(message = "Email invalide")
    private String email;

    private String address;
    private String city;
    private String postalCode;

    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;

    private String insuranceNumber;
    private String insuranceProvider;
}
