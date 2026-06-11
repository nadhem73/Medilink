package com.medilinktunisia.prescriptionservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispenseRequest {

    @NotNull(message = "Pharmacy ID is required")
    private Long pharmacyId;

    @NotNull(message = "Pharmacy name is required")
    private String pharmacyName;

    // Map<medicationId, quantityDispensed>
    @NotNull(message = "Medication quantities are required")
    private Map<Long, Integer> medicationQuantities;
}
