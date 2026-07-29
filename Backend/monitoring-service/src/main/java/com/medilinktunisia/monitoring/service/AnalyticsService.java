package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class AnalyticsService {

    private final MetricsPersistenceService persistenceService;
    private final List<IncidentDto> incidents = new CopyOnWriteArrayList<>();

    public AnalyticsService(MetricsPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @PostConstruct
    public void init() {
        log.info("AnalyticsService initialized — reads from PostgreSQL");
    }

    public void recordIncident(String service, String description, String severity, double resolutionHours) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        incidents.addFirst(IncidentDto.builder()
                .date(date)
                .service(service)
                .description(description)
                .severity(severity)
                .resolutionTimeHours(resolutionHours)
                .build());
        log.warn("Incident recorded: {} - {} ({})", service, description, severity);
    }

    public AnalyticsDto getAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        MetricSummaryDto summary = persistenceService.getSummary(weekAgo, now);

        List<HistoryPointDto> cpuHistory = buildHistoryFromRange("cpu", weekAgo, now);
        List<HistoryPointDto> ramHistory = buildHistoryFromRange("ram", weekAgo, now);
        List<HistoryPointDto> storageHistory = buildHistoryFromRange("disk", weekAgo, now);
        List<HistoryPointDto> uptimeHistory = buildUptimeHistory(weekAgo, now);

        double avgResolution = incidents.stream()
                .mapToDouble(IncidentDto::getResolutionTimeHours)
                .average().orElse(0);

        return AnalyticsDto.builder()
                .uptime30Days(uptimeHistory)
                .cpuEvolution(cpuHistory)
                .ramEvolution(ramHistory)
                .storageEvolution(storageHistory)
                .incidents(new ArrayList<>(incidents))
                .avgResolutionTimeHours(Math.round(avgResolution * 100.0) / 100.0)
                .build();
    }

    private List<HistoryPointDto> buildHistoryFromRange(String metric, LocalDateTime from, LocalDateTime to) {
        List<MetricPointDto> points = persistenceService.getMetricRange(null, metric, from, to);
        Map<String, Double> byDate = new LinkedHashMap<>();
        for (MetricPointDto p : points) {
            String day = p.getTimestamp().substring(0, 10);
            byDate.merge(day, p.getValue(), (a, b) -> (a + b) / 2.0);
        }
        List<HistoryPointDto> result = new ArrayList<>();
        for (var entry : byDate.entrySet()) {
            result.add(HistoryPointDto.builder()
                    .date(entry.getKey())
                    .value(Math.round(entry.getValue() * 100.0) / 100.0)
                    .build());
        }
        return result;
    }

    private List<HistoryPointDto> buildUptimeHistory(LocalDateTime from, LocalDateTime to) {
        LocalDateTime start = from;
        List<HistoryPointDto> result = new ArrayList<>();
        while (!start.isAfter(to)) {
            LocalDateTime dayEnd = start.plusDays(1);
            MetricSummaryDto summary = persistenceService.getSummary(start, dayEnd);
            double uptime = 100.0;
            if (summary.getTotalRequests() != null && summary.getTotalErrors() != null
                    && summary.getTotalRequests() > 0) {
                uptime = 100.0 - (summary.getTotalErrors().doubleValue() / summary.getTotalRequests() * 100.0);
            }
            result.add(HistoryPointDto.builder()
                    .date(start.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .value(Math.round(uptime * 100.0) / 100.0)
                    .build());
            start = dayEnd;
        }
        return result;
    }
}
