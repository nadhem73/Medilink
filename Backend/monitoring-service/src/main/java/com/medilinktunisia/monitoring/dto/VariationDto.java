package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationDto {
    private double activeServices;
    private double uptimePercent;
    private double avgResponseTimeMs;
    private double totalRequestsToday;
    private double errorsToday;
}
