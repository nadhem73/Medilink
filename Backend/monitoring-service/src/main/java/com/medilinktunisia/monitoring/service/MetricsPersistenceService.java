package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.*;
import com.medilinktunisia.monitoring.entity.ServiceHealthHistory;
import com.medilinktunisia.monitoring.repository.MetricsAggregateDao;
import com.medilinktunisia.monitoring.repository.ServiceHealthHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Persistance et lecture des mesures réelles des services.
 * Les compteurs actuator (requêtes, erreurs, notifications) sont cumulés depuis
 * le démarrage de chaque service : les totaux sur une période sont donc calculés
 * par différence entre mesures successives (avec gestion des redémarrages).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsPersistenceService {

    private final ServiceHealthHistoryRepository repository;
    private final PlatformStatsService platformStats;
    private final MetricsAggregateDao aggregates;

    private static final Set<String> KNOWN_SERVICES = Set.of(
        "auth-service", "patient-service", "doctor-service", "pharmacy-service",
        "prescription-service", "bilan-service", "ai-service", "api-gateway"
    );

    public void saveSnapshot(ServiceSample s) {
        repository.save(ServiceHealthHistory.builder()
                .timestamp(LocalDateTime.now())
                .serviceName(s.getServiceName())
                .status(s.getStatus())
                .cpuPercent(s.getCpuPercent())
                .ramPercent(s.getRamPercent())
                .diskPercent(s.getDiskPercent())
                .responseTimeMs(s.getResponseTimeMs())
                .requestsCount(s.getRequestsCount())
                .errorsCount(s.getErrorsCount())
                .uptimeHours(s.getUptimeHours())
                .threadCount(s.getThreadCount())
                .startedAt(s.getStartedAt())
                .version(s.getVersion())
                .notificationsCount(s.getNotificationsCount())
                .build());
    }

    // ── Vue d'ensemble ───────────────────────────────────────────────────────

    public OverviewDto getLatestOverview() {
        List<ServiceHealthHistory> latest = filterKnownServices(repository.findLatestPerService());
        if (latest.isEmpty()) {
            return OverviewDto.builder().variation(VariationDto.builder().build()).build();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDate.now().atStartOfDay();
        WindowStats today = windowStats(midnight, now);

        long online = latest.stream().filter(s -> "Online".equals(s.getStatus())).count();
        long offline = latest.stream().filter(s -> "Offline".equals(s.getStatus())).count();

        double avgResponse = latest.stream()
                .filter(s -> s.getResponseTimeMs() != null && !"Offline".equals(s.getStatus()))
                .mapToDouble(ServiceHealthHistory::getResponseTimeMs)
                .average().orElse(0);

        double disk = latest.stream()
                .filter(s -> s.getDiskPercent() != null)
                .mapToDouble(ServiceHealthHistory::getDiskPercent)
                .max().orElse(0);

        PlatformStatsService.SecurityStats security = platformStats.securityStats();

        return OverviewDto.builder()
                .totalServices(latest.size())
                .activeServices((int) online)
                .offlineServices((int) offline)
                .uptimePercent(round2(today.uptimePercent))
                .avgResponseTimeMs(round2(avgResponse))
                .totalRequestsToday(today.requests)
                .errorsToday(today.errors)
                .connectedUsers((int) security.getActiveSessions())
                .diskPercent(round2(disk))
                .documentsOcrProcessed((int) platformStats.ocrDocuments())
                .notificationsSent((int) today.notifications)
                .variation(computeVariation(today, now))
                .build();
    }

    /** Variation par rapport à la même plage horaire hier. */
    private VariationDto computeVariation(WindowStats today, LocalDateTime now) {
        LocalDateTime yStart = LocalDate.now().minusDays(1).atStartOfDay();
        WindowStats yesterday = windowStats(yStart, now.minusDays(1));
        if (yesterday.snapshots == 0) {
            return VariationDto.builder().build();
        }
        return VariationDto.builder()
                .activeServices(round1(today.lastOnline - yesterday.lastOnline))
                .uptimePercent(round1(today.uptimePercent - yesterday.uptimePercent))
                .avgResponseTimeMs(round1(today.avgResponse - yesterday.avgResponse))
                .totalRequestsToday(round1(today.requests - yesterday.requests))
                .errorsToday(round1(today.errors - yesterday.errors))
                .build();
    }

    private static class WindowStats {
        long snapshots;
        double uptimePercent;
        double avgResponse;
        long requests;
        long errors;
        long notifications;
        long lastOnline;
    }

    private WindowStats windowStats(LocalDateTime from, LocalDateTime to) {
        WindowStats w = new WindowStats();
        MetricsAggregateDao.StatusStats status = aggregates.statusStats(from, to);
        w.snapshots = status.snapshots();
        if (w.snapshots == 0) return w;
        w.uptimePercent = status.online() * 100.0 / status.snapshots();
        w.avgResponse = status.avgResponse();
        w.lastOnline = status.lastOnline();

        MetricsAggregateDao.CounterTotals totals = aggregates.counterTotals(from, to);
        w.requests = totals.requests();
        w.errors = totals.errors();
        w.notifications = totals.notifications();
        return w;
    }

    // ── Services ────────────────────────────────────────────────────────────

    public List<ServiceHealthDto> getLatestServices() {
        List<ServiceHealthHistory> latest = filterKnownServices(repository.findLatestPerService());
        latest.sort(Comparator.comparing(ServiceHealthHistory::getServiceName));
        return latest.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Série réelle, moyennée par intervalle (300 points max) pour les graphiques. */
    public List<MetricPointDto> getMetricRange(String serviceName, String metric,
                                               LocalDateTime from, LocalDateTime to) {
        String column = MetricsAggregateDao.column(metric);
        if (column == null) return List.of();
        return aggregates.bucketed(column, serviceName, from, to, 300).stream()
                .map(p -> MetricPointDto.builder()
                        .timestamp(p.at().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .value(round2(p.value()))
                        .serviceName(p.serviceName())
                        .build())
                .collect(Collectors.toList());
    }

    public MetricSummaryDto getSummary(LocalDateTime from, LocalDateTime to) {
        List<Object[]> results = repository.aggregateMetricsBetween(from, to);
        if (results.isEmpty() || results.get(0)[0] == null) {
            return MetricSummaryDto.builder().build();
        }
        Object[] row = results.get(0);
        // Les sommes SQL de compteurs cumulés n'ont pas de sens : on calcule les vrais totaux
        WindowStats w = windowStats(from, to);
        return MetricSummaryDto.builder()
                .avgCpu(toDouble(row[0]))
                .avgRam(toDouble(row[1]))
                .avgDisk(toDouble(row[2]))
                .avgResponseTimeMs(toDouble(row[3]))
                .totalRequests(w.requests)
                .totalErrors(w.errors)
                .build();
    }

    public String exportCsv(LocalDateTime from, LocalDateTime to) {
        List<ServiceHealthHistory> records = repository.findByTimestampBetweenOrderByTimestampAsc(from, to);
        StringBuilder sb = new StringBuilder();
        sb.append("timestamp,serviceName,status,cpuPercent,ramPercent,diskPercent,")
          .append("responseTimeMs,requestsCount,errorsCount,uptimeHours,threadCount,startedAt,notificationsCount\n");
        for (ServiceHealthHistory r : records) {
            sb.append(r.getTimestamp()).append(',')
              .append(escapeCsv(r.getServiceName())).append(',')
              .append(r.getStatus()).append(',')
              .append(nz(r.getCpuPercent())).append(',')
              .append(nz(r.getRamPercent())).append(',')
              .append(nz(r.getDiskPercent())).append(',')
              .append(nz(r.getResponseTimeMs())).append(',')
              .append(nz(r.getRequestsCount())).append(',')
              .append(nz(r.getErrorsCount())).append(',')
              .append(nz(r.getUptimeHours())).append(',')
              .append(nz(r.getThreadCount())).append(',')
              .append(nz(r.getStartedAt())).append(',')
              .append(nz(r.getNotificationsCount())).append('\n');
        }
        return sb.toString();
    }

    public String exportPrometheus() {
        List<ServiceHealthHistory> latest = filterKnownServices(repository.findLatestPerService());
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP medilink_service_cpu Process CPU usage percentage per service\n");
        sb.append("# TYPE medilink_service_cpu gauge\n");
        sb.append("# HELP medilink_service_ram JVM heap usage percentage per service\n");
        sb.append("# TYPE medilink_service_ram gauge\n");
        sb.append("# HELP medilink_service_disk Disk usage percentage per service\n");
        sb.append("# TYPE medilink_service_disk gauge\n");
        sb.append("# HELP medilink_service_response_ms Health check response time in ms\n");
        sb.append("# TYPE medilink_service_response_ms gauge\n");
        sb.append("# HELP medilink_service_requests_total HTTP requests served since start\n");
        sb.append("# TYPE medilink_service_requests_total counter\n");
        sb.append("# HELP medilink_service_errors_total HTTP 5xx responses since start\n");
        sb.append("# TYPE medilink_service_errors_total counter\n");
        sb.append("# HELP medilink_service_info Service status info\n");
        sb.append("# TYPE medilink_service_info gauge\n");

        for (ServiceHealthHistory s : latest) {
            String name = s.getServiceName().toLowerCase().replace(' ', '_');
            appendGauge(sb, "medilink_service_cpu", name, s.getCpuPercent());
            appendGauge(sb, "medilink_service_ram", name, s.getRamPercent());
            appendGauge(sb, "medilink_service_disk", name, s.getDiskPercent());
            appendGauge(sb, "medilink_service_response_ms", name, s.getResponseTimeMs());
            if (s.getRequestsCount() != null) {
                sb.append(String.format("medilink_service_requests_total{service=\"%s\"} %d\n", name, s.getRequestsCount()));
            }
            if (s.getErrorsCount() != null) {
                sb.append(String.format("medilink_service_errors_total{service=\"%s\"} %d\n", name, s.getErrorsCount()));
            }
            int statusVal = "Online".equals(s.getStatus()) ? 1 : "Degraded".equals(s.getStatus()) ? 0 : -1;
            sb.append(String.format("medilink_service_info{service=\"%s\",status=\"%s\",version=\"%s\"} %d\n",
                    name, s.getStatus(), s.getVersion() != null ? s.getVersion() : "", statusVal));
        }
        sb.append("# EOF\n");
        return sb.toString();
    }

    private static void appendGauge(StringBuilder sb, String metric, String service, Double value) {
        if (value != null) {
            sb.append(String.format(Locale.ROOT, "%s{service=\"%s\"} %.2f\n", metric, service, value));
        }
    }

    private ServiceHealthDto toDto(ServiceHealthHistory h) {
        return ServiceHealthDto.builder()
                .name(formatDisplayName(h.getServiceName()))
                .status(h.getStatus() != null ? h.getStatus() : "Unknown")
                .cpuPercent(h.getCpuPercent() != null ? h.getCpuPercent() : 0)
                .ramPercent(h.getRamPercent() != null ? h.getRamPercent() : 0)
                .responseTimeMs(h.getResponseTimeMs() != null ? h.getResponseTimeMs() : 0)
                .requestsCount(h.getRequestsCount() != null ? h.getRequestsCount() : 0)
                .errorsCount(h.getErrorsCount() != null ? h.getErrorsCount() : 0)
                .uptimeHours(h.getUptimeHours() != null ? h.getUptimeHours() : 0)
                .lastRestart(h.getStartedAt() != null ? h.getStartedAt().withNano(0).toString() : "")
                .version(h.getVersion() != null ? h.getVersion() : "")
                .build();
    }

    private List<ServiceHealthHistory> filterKnownServices(List<ServiceHealthHistory> records) {
        return records.stream()
                .filter(r -> KNOWN_SERVICES.contains(r.getServiceName()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String nz(Object o) {
        return o == null ? "" : o.toString();
    }

    private static double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }

    private static double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    public static String formatDisplayName(String name) {
        if (name == null) return "Unknown";
        return switch (name) {
            case "auth-service" -> "Auth Service";
            case "patient-service" -> "Patient Service";
            case "doctor-service" -> "Doctor Service";
            case "pharmacy-service" -> "Pharmacy Service";
            case "prescription-service" -> "Prescription Service";
            case "bilan-service" -> "Bilan Service";
            case "ai-service" -> "AI Service";
            case "api-gateway" -> "API Gateway";
            default -> name;
        };
    }

    private Double toDouble(Object obj) {
        if (obj instanceof Number n) return Math.round(n.doubleValue() * 100.0) / 100.0;
        return null;
    }
}
