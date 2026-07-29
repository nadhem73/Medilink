package com.medilinktunisia.monitoring.scheduler;

import com.medilinktunisia.monitoring.dto.OverviewDto;
import com.medilinktunisia.monitoring.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsPollingScheduler {

    private final MetricsAggregatorService metricsAggregatorService;
    private final AlertService alertService;
    private final MetricsPersistenceService persistenceService;
    private final SseService sseService;

    @Scheduled(fixedDelayString = "${monitoring.polling.interval-ms}")
    public void pollMetrics() {
        log.debug("Polling metrics from all services...");

        metricsAggregatorService.pollAllServices();
        alertService.evaluateThresholds();

        OverviewDto overview = persistenceService.getLatestOverview();
        sseService.broadcast("overview", overview);
        sseService.broadcast("services", persistenceService.getLatestServices());
    }
}
