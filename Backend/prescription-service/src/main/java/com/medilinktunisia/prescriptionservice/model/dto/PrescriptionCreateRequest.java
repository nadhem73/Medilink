package com.medilinktunisia.prescriptionservice.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionCreateRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    @NotBlank(message = "Doctor specialty is required")
    private String doctorSpecialty;

    private String diagnosis;

    private String notes;

    @NotEmpty(message = "At least one medication is required")
    @Valid
    private List<MedicationItemDto> medications;

    private LocalDateTime prescriptionDate;

    private LocalDateTime expiryDate;
}
