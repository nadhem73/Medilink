package com.medilinktunisia.patientservice.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreateRequest {

    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long doctorId;

    @NotNull(message = "La date du rendez-vous est obligatoire")
    @Future(message = "La date doit être dans le futur")
    private LocalDate appointmentDate;

    @NotNull(message = "L'heure du rendez-vous est obligatoire")
    private LocalTime appointmentTime;

    private Integer estimatedDurationMinutes;

    private String appointmentType; // CONSULTATION, FOLLOW_UP, EMERGENCY, TELECONSULTATION

    @Size(max = 500, message = "Le motif ne doit pas dépasser 500 caractères")
    private String appointmentReason;
}
