package com.medilinktunisia.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSummaryDto {
    private Double avgCpu;
    private Double avgRam;
    private Double avgDisk;
    private Double avgResponseTimeMs;
    private Long totalRequests;
    private Long totalErrors;
}
