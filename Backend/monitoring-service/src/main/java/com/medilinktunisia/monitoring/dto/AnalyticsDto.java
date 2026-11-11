package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDto {
    private List<HistoryPointDto> uptime30Days;
    private List<HistoryPointDto> cpuEvolution;
    private List<HistoryPointDto> ramEvolution;
    private List<HistoryPointDto> storageEvolution;
    private List<IncidentDto> incidents;
    private double avgResolutionTimeHours;
}
