package com.medilinktunisia.prescriptionservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PrescriptionCreateRequest {

    @NotNull
    private Long consultationId;

    @NotNull
    private Long patientId;

    private String notes;

    @NotEmpty
    @Valid
    private List<PrescriptionItemRequest> items;
}
