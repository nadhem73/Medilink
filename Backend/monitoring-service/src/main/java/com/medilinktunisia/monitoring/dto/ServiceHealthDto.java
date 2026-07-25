package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDto {
    private String name;
    private String status;
    private double cpuPercent;
    private double ramPercent;
    private double responseTimeMs;
    private long requestsCount;
    private long errorsCount;
    private double uptimeHours;
    private String lastRestart;
    private String version;
}
