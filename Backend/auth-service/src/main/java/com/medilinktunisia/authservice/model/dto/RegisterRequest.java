package com.medilinktunisia.authservice.model.dto;

import com.medilinktunisia.authservice.model.entity.Role;
import com.medilinktunisia.authservice.model.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    // ==================== CHAMPS COMMUNS ====================
    
    @NotBlank(message = "Email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String firstName;

    @NotBlank(message = "Nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String lastName;

    @NotBlank(message = "Téléphone est obligatoire")
    @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
    private String phone;

    @NotBlank(message = "Adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String address;

    @NotNull(message = "Date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;

    @NotNull(message = "Sexe est obligatoire")
    private User.Gender gender;

    @Size(max = 20, message = "Le CIN ne doit pas dépasser 20 caractères")
    private String cin;

    @NotNull(message = "Le rôle est obligatoire")
    private Role.RoleName role;

    // ==================== CHAMPS SPÉCIFIQUES PATIENT ====================
    
    private String bloodGroup;
    private BigDecimal height;
    private BigDecimal weight;
    private String allergies;
    private String chronicDiseases;
    private String currentTreatments;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String insuranceCompany;
    private String insuranceNumber;

    // ==================== CHAMPS SPÉCIFIQUES DOCTOR ====================
    
    private String medicalLicenseNumber;
    private String specialty;
    private String subSpecialty;
    private Integer yearsOfExperience;
    private String diploma;
    private String university;
    private String hospital;
    private String officeAddress;
    private String consultationHours;
    private BigDecimal consultationFees;
    private String biography;
    private String languages;

    // ==================== CHAMPS SPÉCIFIQUES PHARMACIST ====================
    
    private String pharmacyLicenseNumber;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyPhone;
    private String openingHours;
    private Boolean hasDelivery;

    // ==================== CHAMPS SPÉCIFIQUES LABORATORY ====================
    
    private String laboratoryName;
    private String laboratoryLicenseNumber;
    private String responsibleName;
    private String professionalEmail;
    private String analysisTypes;
    private Boolean hasHomeService;

    // ==================== CHAMPS SPÉCIFIQUES AMBULANCE ====================
    
    private String companyName;
    private String ambulanceLicenseNumber;
    private String emergencyPhone;
    private Integer numberOfAmbulances;
    private String coverageArea;
    private String ambulanceType;
    private Boolean available24h;
}
