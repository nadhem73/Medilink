package com.medilinktunisia.ambulanceservice.model.dto;

import com.medilinktunisia.ambulanceservice.model.enums.EmergencyPriority;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyCreateRequest {

    @NotBlank(message = "Le nom de l'appelant est obligatoire")
    private String callerName;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^[0-9]{8}$", message = "Le numéro de téléphone doit contenir 8 chiffres")
    private String callerPhone;

    private String callerRelationToPatient;

    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private Long patientId;

    @NotNull(message = "Le type d'urgence est obligatoire")
    private EmergencyType emergencyType;

    @NotNull(message = "La priorité est obligatoire")
    private EmergencyPriority priority;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private String symptoms;

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @NotNull(message = "La latitude est obligatoire")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    private Double longitude;

    private String locationDetails;
}
