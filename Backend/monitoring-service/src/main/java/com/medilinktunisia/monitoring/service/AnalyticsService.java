package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.AnalyticsDto;
import com.medilinktunisia.monitoring.dto.HistoryPointDto;
import com.medilinktunisia.monitoring.dto.IncidentDto;
import com.medilinktunisia.monitoring.repository.MetricsAggregateDao;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rapports réels : moyennes journalières mesurées, disponibilité calculée sur
 * les health checks, et incidents reconstitués à partir des périodes où un
 * service était hors ligne ou dégradé.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MetricsAggregateDao aggregates;

    @Value("${monitoring.polling.interval-ms:10000}")
    private long pollingIntervalMs;

    public AnalyticsDto getAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusDays(30);

        List<IncidentDto> incidents = buildIncidents(monthAgo, now);
        double avgResolution = incidents.stream()
                .mapToDouble(IncidentDto::getResolutionTimeHours)
                .average().orElse(0);

        return AnalyticsDto.builder()
                .uptime30Days(toHistory(aggregates.dailyUptime(monthAgo, now)))
                .cpuEvolution(toHistory(aggregates.dailyAverage("cpu_percent", weekAgo, now)))
                .ramEvolution(toHistory(aggregates.dailyAverage("ram_percent", weekAgo, now)))
                .storageEvolution(toHistory(aggregates.dailyAverage("disk_percent", weekAgo, now)))
                .incidents(incidents)
                .avgResolutionTimeHours(round2(avgResolution))
                .build();
    }

    private List<HistoryPointDto> toHistory(List<MetricsAggregateDao.DayValue> values) {
        return values.stream()
                .map(v -> HistoryPointDto.builder()
                        .date(v.date().toString())
                        .value(round2(v.value()))
                        .build())
                .toList();
    }

    /**
     * Un incident = suite continue de mesures non « Online » pour un même service.
     * Deux mesures séparées de plus de 3 intervalles de polling ouvrent un nouvel incident.
     */
    private List<IncidentDto> buildIncidents(LocalDateTime from, LocalDateTime to) {
        List<MetricsAggregateDao.StatusPoint> points = aggregates.nonOnline(from, to);
        List<IncidentDto> incidents = new ArrayList<>();
        Duration gap = Duration.ofMillis(pollingIntervalMs * 3);

        int i = 0;
        while (i < points.size()) {
            MetricsAggregateDao.StatusPoint first = points.get(i);
            MetricsAggregateDao.StatusPoint last = first;
            boolean offline = "Offline".equals(first.status());
            int j = i + 1;
            while (j < points.size()
                    && points.get(j).serviceName().equals(first.serviceName())
                    && Duration.between(last.at(), points.get(j).at()).compareTo(gap) <= 0) {
                last = points.get(j);
                offline |= "Offline".equals(last.status());
                j++;
            }

            LocalDateTime end = aggregates.firstOnlineAfter(first.serviceName(), last.at());
            boolean ongoing = end == null;
            LocalDateTime effectiveEnd = ongoing ? to : end;
            double hours = Duration.between(first.at(), effectiveEnd).toSeconds() / 3600.0;

            String name = MetricsPersistenceService.formatDisplayName(first.serviceName());
            String description = (offline ? "Service injoignable" : "Service dégradé")
                    + (ongoing ? " (en cours)" : "")
                    + " depuis " + first.at().format(DateTimeFormatter.ofPattern("HH:mm"));
            incidents.add(IncidentDto.builder()
                    .date(first.at().toLocalDate().toString())
                    .service(name)
                    .description(description)
                    .severity(severity(offline, hours))
                    .resolutionTimeHours(round2(hours))
                    .build());
            i = j;
        }
        incidents.sort(Comparator.comparing(IncidentDto::getDate).reversed());
        return incidents;
    }

    private static String severity(boolean offline, double hours) {
        if (offline && hours >= 1) return "CRITICAL";
        if (offline) return "HIGH";
        return hours >= 1 ? "MEDIUM" : "LOW";
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
