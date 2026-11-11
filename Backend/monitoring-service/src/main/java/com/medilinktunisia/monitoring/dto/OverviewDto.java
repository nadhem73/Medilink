package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewDto {
    private int totalServices;
    private int activeServices;
    private int offlineServices;
    private double uptimePercent;
    private double avgResponseTimeMs;
    private long totalRequestsToday;
    private long errorsToday;
    private int connectedUsers;
    private double diskPercent;
    private int documentsOcrProcessed;
    private int notificationsSent;
    private VariationDto variation;
}
