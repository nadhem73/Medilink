package com.medilinktunisia.laboratoryservice.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboratoryCreateRequest {

    @NotNull(message = "User ID est obligatoire")
    private Long userId;

    @NotBlank(message = "Le nom du laboratoire est obligatoire")
    @Size(min = 3, max = 200, message = "Le nom doit contenir entre 3 et 200 caractères")
    private String name;

    @NotBlank(message = "Le numéro de licence est obligatoire")
    @Size(max = 50, message = "Le numéro de licence ne peut pas dépasser 50 caractères")
    private String licenseNumber;

    @NotBlank(message = "Le nom du directeur est obligatoire")
    private String directorName;

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotBlank(message = "Le code postal est obligatoire")
    @Pattern(regexp = "\\d{4}", message = "Le code postal doit contenir 4 chiffres")
    private String zipCode;

    private String region;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^[0-9]{8}$", message = "Le téléphone doit contenir 8 chiffres")
    private String phone;

    @Pattern(regexp = "^[0-9]{8}$", message = "Le téléphone alternatif doit contenir 8 chiffres")
    private String alternativePhone;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0", message = "Latitude invalide")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0", message = "Longitude invalide")
    private Double longitude;

    private String openingHours;

    private String accreditations;

    private String specialties;

    private Boolean homeCollection;

    private Boolean urgentAnalysisAvailable;

    private String description;

    @Pattern(regexp = "^(https?://)?[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(:[0-9]+)?(/.*)?$", 
             message = "Format d'URL invalide")
    private String website;

    @Min(value = 1, message = "Le délai moyen doit être supérieur à 0")
    private Integer averageTurnaroundTimeHours;
}
