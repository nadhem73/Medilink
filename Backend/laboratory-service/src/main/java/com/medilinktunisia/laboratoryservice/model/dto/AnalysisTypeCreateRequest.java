package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisTypeCreateRequest {

    @NotBlank(message = "Analysis code is required")
    @Size(max = 20)
    private String analysisCode;

    @NotBlank(message = "Analysis name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Category is required")
    private AnalysisType.AnalysisCategory category;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Base price must be positive")
    private BigDecimal basePrice;

    @Size(max = 50)
    private String unit;

    @Size(max = 200)
    private String referenceRange;

    @Min(value = 1, message = "Estimated duration must be at least 1 hour")
    private Integer estimatedDuration; // en heures

    @NotBlank(message = "Sample type is required")
    @Size(max = 100)
    private String sampleType;

    @Size(max = 1000)
    private String preparationInstructions;

    private Boolean fastingRequired;

    @Builder.Default
    private Boolean active = true;
}
