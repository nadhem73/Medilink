package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {
    private String date;
    private String service;
    private String description;
    private String severity;
    private double resolutionTimeHours;
}
