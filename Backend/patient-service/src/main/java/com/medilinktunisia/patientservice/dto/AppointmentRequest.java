package com.medilinktunisia.patientservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {

    @NotNull(message = "Le médecin est obligatoire")
    private Long doctorId;

    @NotNull(message = "La date et l'heure du rendez-vous sont obligatoires")
    private LocalDateTime dateTime;

    @NotNull(message = "Le mode de consultation est obligatoire")
    private String mode; // PRESENTIEL / TELECONSULTATION

    private String notes;
}
