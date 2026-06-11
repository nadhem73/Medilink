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
public class LaboratoryUpdateRequest {

    @Size(min = 3, max = 200, message = "Le nom doit contenir entre 3 et 200 caractères")
    private String name;

    private String directorName;

    private String address;

    private String city;

    @Pattern(regexp = "\\d{4}", message = "Le code postal doit contenir 4 chiffres")
    private String zipCode;

    private String region;

    @Pattern(regexp = "^[0-9]{8}$", message = "Le téléphone doit contenir 8 chiffres")
    private String phone;

    @Pattern(regexp = "^[0-9]{8}$", message = "Le téléphone alternatif doit contenir 8 chiffres")
    private String alternativePhone;

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
