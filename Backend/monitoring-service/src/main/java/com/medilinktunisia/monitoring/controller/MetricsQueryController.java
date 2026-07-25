package com.medilinktunisia.monitoring.controller;

import com.medilinktunisia.monitoring.dto.*;
import com.medilinktunisia.monitoring.service.MetricsPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/monitoring/metrics")
@RequiredArgsConstructor
public class MetricsQueryController {

    private final MetricsPersistenceService persistenceService;

    private static LocalDateTime parseDateTime(String value) {
        if (value == null) return null;
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            String cleaned = value.replace("Z", "").replace("z", "");
            return LocalDateTime.parse(cleaned);
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<OverviewDto> getLatest() {
        return ResponseEntity.ok(persistenceService.getLatestOverview());
    }

    @GetMapping("/services")
    public ResponseEntity<?> getLatestServices() {
        return ResponseEntity.ok(persistenceService.getLatestServices());
    }

    @GetMapping("/range")
    public ResponseEntity<?> getMetricRange(
            @RequestParam(required = false) String service,
            @RequestParam String metric,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDt = from != null ? parseDateTime(from) : LocalDateTime.now().minusHours(1);
        LocalDateTime toDt = to != null ? parseDateTime(to) : LocalDateTime.now();
        return ResponseEntity.ok(persistenceService.getMetricRange(service, metric, fromDt, toDt));
    }

    @GetMapping("/summary")
    public ResponseEntity<MetricSummaryDto> getSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDt = from != null ? parseDateTime(from) : LocalDateTime.now().minusHours(1);
        LocalDateTime toDt = to != null ? parseDateTime(to) : LocalDateTime.now();
        return ResponseEntity.ok(persistenceService.getSummary(fromDt, toDt));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<Resource> exportCsv(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        LocalDateTime fromDt = from != null ? parseDateTime(from) : LocalDateTime.now().minusDays(1);
        LocalDateTime toDt = to != null ? parseDateTime(to) : LocalDateTime.now();
        String csv = persistenceService.exportCsv(fromDt, toDt);
        ByteArrayResource resource = new ByteArrayResource(csv.getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=metrics-export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @GetMapping(value = "/prometheus", produces = "text/plain; charset=utf-8")
    public ResponseEntity<String> getPrometheusMetrics() {
        return ResponseEntity.ok(persistenceService.exportPrometheus());
    }
}
