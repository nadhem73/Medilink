package com.medilinktunisia.ambulanceservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDto {
    private Long totalAmbulances;
    private Long availableAmbulances;
    private Long onMissionAmbulances;
    private Long totalEmergenciesToday;
    private Long activeEmergencies;
    private Long completedEmergenciesToday;
    private Double averageResponseTimeMinutes;
    private Long criticalEmergencies;
    private Long highPriorityEmergencies;
}
