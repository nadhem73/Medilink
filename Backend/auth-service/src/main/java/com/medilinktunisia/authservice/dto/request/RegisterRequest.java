package com.medilinktunisia.authservice.dto.request;

import com.medilinktunisia.authservice.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Requête d'inscription d'un patient.
 * Reçoit tous les champs envoyés par le formulaire frontend :
 * identité + données médicales (ces dernières sont transmises au patient-service).
 */
@Data
public class RegisterRequest {

    // ---- Identité (auth-service) ----
    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;

    private LocalDate birthDate;

    private Gender gender;

    @Size(max = 255)
    private String address;

    @NotBlank
    @Size(max = 20)
    private String cin;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    /** Rôle fixé à PATIENT par le frontend ; ignoré côté serveur (sécurité). */
    private String role;

    // ---- Données médicales (transmises au patient-service) ----
    private String bloodGroup;
    private Double height;
    private Double weight;
    private String allergies;
    private String chronicDiseases;
    private String currentTreatments;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String insuranceCompany;
    private String insuranceNumber;
}
