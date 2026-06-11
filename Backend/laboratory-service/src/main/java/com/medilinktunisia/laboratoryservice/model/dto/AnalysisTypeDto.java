package com.medilinktunisia.laboratoryservice.model.dto;

import com.medilinktunisia.laboratoryservice.model.entity.AnalysisType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisTypeDto {
    private Long id;
    private String analysisCode;
    private String name;
    private String description;
    private AnalysisType.AnalysisCategory category;
    private BigDecimal basePrice;
    private String unit;
    private String referenceRange;
    private Integer estimatedDuration; // en heures
    private String sampleType;
    private String preparationInstructions;
    private Boolean fastingRequired;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
