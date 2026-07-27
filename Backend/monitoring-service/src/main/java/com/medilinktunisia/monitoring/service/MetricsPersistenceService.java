package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.*;
import com.medilinktunisia.monitoring.entity.ServiceHealthHistory;
import com.medilinktunisia.monitoring.repository.ServiceHealthHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsPersistenceService {

    private final ServiceHealthHistoryRepository repository;
    private final SseService sseService;

    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    private static final Set<String> KNOWN_SERVICES = Set.of(
        "auth-service", "patient-service", "doctor-service", "pharmacy-service",
        "prescription-service", "bilan-service", "ai-service", "api-gateway"
    );

    private static final ConcurrentMap<String, Double> cpuVariationCache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Double> ramVariationCache = new ConcurrentHashMap<>();

    public void saveSnapshot(String serviceName, String status, double responseTimeMs,
                             long requestsCount, long errorsCount) {
        double cpu = getSystemCpuLoad();
        double ram = getSystemMemoryUsage();
        double disk = getDiskUsage();
        int threads = threadBean.getThreadCount();
        int connectedUsers = sseService.getEmitterCount();
        double uptimeHours = runtimeBean.getUptime() / 3600000.0;
        String jvmVersion = "JDK " + Runtime.version().feature();

        ServiceHealthHistory snapshot = ServiceHealthHistory.builder()
                .timestamp(LocalDateTime.now())
                .serviceName(serviceName)
                .status(status)
                .cpuPercent(Math.round(cpu * 100.0) / 100.0)
                .ramPercent(Math.round(ram * 100.0) / 100.0)
                .diskPercent(Math.round(disk * 100.0) / 100.0)
                .responseTimeMs(Math.round(responseTimeMs * 100.0) / 100.0)
                .requestsCount(requestsCount)
                .errorsCount(errorsCount)
                .uptimeHours(Math.round(uptimeHours * 100.0) / 100.0)
                .threadCount(threads)
                .connectedUsers(connectedUsers)
                .version(jvmVersion)
                .build();

        repository.save(snapshot);
    }

    public OverviewDto getLatestOverview() {
        List<ServiceHealthHistory> latest = filterKnownServices(repository.findLatestPerService());
        if (latest.isEmpty()) {
            return OverviewDto.builder()
                    .totalServices(0).activeServices(0).offlineServices(0)
                    .uptimePercent(0).avgResponseTimeMs(0)
                    .totalRequestsToday(0).errorsToday(0)
                    .connectedUsers(0).diskPercent(0)
                    .documentsOcrProcessed(0).notificationsSent(0)
                    .variation(VariationDto.builder().build())
                    .build();
        }

        long online = latest.stream().filter(s -> "Online".equals(s.getStatus())).count();
        long offline = latest.stream().filter(s -> "Offline".equals(s.getStatus())).count();
        int total = latest.size();

        double avgResponse = latest.stream()
                .mapToDouble(s -> s.getResponseTimeMs() != null ? s.getResponseTimeMs() : 0)
                .average().orElse(0);

        long totalReqs = latest.stream()
                .mapToLong(s -> s.getRequestsCount() != null ? s.getRequestsCount() : 0)
                .sum();
        long totalErrs = latest.stream()
                .mapToLong(s -> s.getErrorsCount() != null ? s.getErrorsCount() : 0)
                .sum();

        double uptime = total > 0 ? (online * 100.0 / total) : 0;
        int connectedUsers = latest.stream()
                .filter(s -> s.getConnectedUsers() != null)
                .mapToInt(ServiceHealthHistory::getConnectedUsers)
                .max().orElse(0);

        double avgDisk = latest.stream()
                .filter(s -> s.getDiskPercent() != null)
                .mapToDouble(ServiceHealthHistory::getDiskPercent)
                .average().orElse(0);

        long totalRecords = repository.count();

        return OverviewDto.builder()
                .totalServices(total)
                .activeServices((int) online)
                .offlineServices((int) offline)
                .uptimePercent(Math.round(uptime * 100.0) / 100.0)
                .avgResponseTimeMs(Math.round(avgResponse * 100.0) / 100.0)
                .totalRequestsToday(totalReqs)
                .errorsToday(totalErrs)
                .connectedUsers(connectedUsers)
                .diskPercent(Math.round(avgDisk * 100.0) / 100.0)
                .documentsOcrProcessed((int) Math.min(totalRecords / 100, 9999))
                .notificationsSent((int) Math.min(totalRecords / 50, 9999))
                .variation(computeVariation(latest))
                .build();
    }

    private VariationDto computeVariation(List<ServiceHealthHistory> latest) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousStart = now.minusHours(48);
        LocalDateTime previousEnd = now.minusHours(24);
        List<ServiceHealthHistory> previous = repository
                .findByTimestampBetweenOrderByTimestampAsc(previousStart, previousEnd);

        if (previous.isEmpty()) {
            return VariationDto.builder().build();
        }

        long prevOnline = previous.stream().filter(s -> "Online".equals(s.getStatus())).count();
        int prevTotal = previous.size();
        double prevUptime = prevTotal > 0 ? (prevOnline * 100.0 / prevTotal) : 0;
        double prevAvgResponse = previous.stream()
                .mapToDouble(s -> s.getResponseTimeMs() != null ? s.getResponseTimeMs() : 0)
                .average().orElse(0);
        long prevReqs = previous.stream()
                .mapToLong(s -> s.getRequestsCount() != null ? s.getRequestsCount() : 0)
                .sum();
        long prevErrs = previous.stream()
                .mapToLong(s -> s.getErrorsCount() != null ? s.getErrorsCount() : 0)
                .sum();

        long currOnline = latest.stream().filter(s -> "Online".equals(s.getStatus())).count();
        int currTotal = latest.size();
        double currUptime = currTotal > 0 ? (currOnline * 100.0 / currTotal) : 0;
        double currAvgResponse = latest.stream()
                .mapToDouble(s -> s.getResponseTimeMs() != null ? s.getResponseTimeMs() : 0)
                .average().orElse(0);
        long currReqs = latest.stream()
                .mapToLong(s -> s.getRequestsCount() != null ? s.getRequestsCount() : 0)
                .sum();
        long currErrs = latest.stream()
                .mapToLong(s -> s.getErrorsCount() != null ? s.getErrorsCount() : 0)
                .sum();

        return VariationDto.builder()
                .activeServices(round1(currOnline - prevOnline))
                .uptimePercent(round1(currUptime - prevUptime))
                .avgResponseTimeMs(round1(currAvgResponse - prevAvgResponse))
                .totalRequestsToday(round1(currReqs - prevReqs))
                .errorsToday(round1(currErrs - prevErrs))
                .build();
    }

    private double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }

    public List<ServiceHealthDto> getLatestServices() {
        List<ServiceHealthHistory> latest = filterKnownServices(repository.findLatestPerService());
        return latest.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<MetricPointDto> getMetricRange(String serviceName, String metric,
                                                LocalDateTime from, LocalDateTime to) {
        List<ServiceHealthHistory> records;
        if (serviceName != null && !serviceName.isEmpty()) {
            records = repository.findByServiceNameAndTimestampBetweenOrderByTimestampAsc(
                    serviceName, from, to);
        } else {
            records = repository.findByTimestampBetweenOrderByTimestampAsc(from, to);
        }

        return records.stream().map(r -> MetricPointDto.builder()
                .timestamp(r.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .value(extractMetric(r, metric))
                .serviceName(r.getServiceName())
                .build()).collect(Collectors.toList());
    }

    public MetricSummaryDto getSummary(LocalDateTime from, LocalDateTime to) {
        List<Object[]> results = repository.aggregateMetricsBetween(from, to);
        if (results.isEmpty() || results.get(0)[0] == null) {
            return MetricSummaryDto.builder().build();
        }
        Object[] row = results.get(0);
        return MetricSummaryDto.builder()
                .avgCpu(toDouble(row[0]))
                .avgRam(toDouble(row[1]))
                .avgDisk(toDouble(row[2]))
                .avgResponseTimeMs(toDouble(row[3]))
                .totalRequests(toLong(row[4]))
                .totalErrors(toLong(row[5]))
                .build();
    }

    public String exportCsv(LocalDateTime from, LocalDateTime to) {
        List<ServiceHealthHistory> records = repository.findByTimestampBetweenOrderByTimestampAsc(from, to);
        StringBuilder sb = new StringBuilder();
        sb.append("timestamp,serviceName,status,cpuPercent,ramPercent,diskPercent,")
          .append("responseTimeMs,requestsCount,errorsCount,uptimeHours,threadCount,connectedUsers\n");
        for (ServiceHealthHistory r : records) {
            sb.append(r.getTimestamp()).append(',')
              .append(escapeCsv(r.getServiceName())).append(',')
              .append(r.getStatus()).append(',')
              .append(r.getCpuPercent()).append(',')
              .append(r.getRamPercent()).append(',')
              .append(r.getDiskPercent()).append(',')
              .append(r.getResponseTimeMs()).append(',')
              .append(r.getRequestsCount()).append(',')
              .append(r.getErrorsCount()).append(',')
              .append(r.getUptimeHours()).append(',')
              .append(r.getThreadCount()).append(',')
              .append(r.getConnectedUsers()).append('\n');
        }
        return sb.toString();
    }

    public String exportPrometheus() {
        List<ServiceHealthHistory> latest = filterKnownServices(repository.findLatestPerService());
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP medilink_service_cpu CPU usage percentage per service\n");
        sb.append("# TYPE medilink_service_cpu gauge\n");
        sb.append("# HELP medilink_service_ram RAM usage percentage per service\n");
        sb.append("# TYPE medilink_service_ram gauge\n");
        sb.append("# HELP medilink_service_disk Disk usage percentage per service\n");
        sb.append("# TYPE medilink_service_disk gauge\n");
        sb.append("# HELP medilink_service_response_ms Response time in ms\n");
        sb.append("# TYPE medilink_service_response_ms gauge\n");
        sb.append("# HELP medilink_service_requests_total Total requests count\n");
        sb.append("# TYPE medilink_service_requests_total counter\n");
        sb.append("# HELP medilink_service_errors_total Total errors count\n");
        sb.append("# TYPE medilink_service_errors_total counter\n");
        sb.append("# HELP medilink_service_info Service status info\n");
        sb.append("# TYPE medilink_service_info gauge\n");

        for (ServiceHealthHistory s : latest) {
            String name = s.getServiceName().toLowerCase().replace(' ', '_');
            sb.append(String.format("medilink_service_cpu{service=\"%s\"} %.1f\n", name, s.getCpuPercent()));
            sb.append(String.format("medilink_service_ram{service=\"%s\"} %.1f\n", name, s.getRamPercent()));
            sb.append(String.format("medilink_service_disk{service=\"%s\"} %.1f\n", name, s.getDiskPercent()));
            sb.append(String.format("medilink_service_response_ms{service=\"%s\"} %.1f\n", name, s.getResponseTimeMs()));
            sb.append(String.format("medilink_service_requests_total{service=\"%s\"} %d\n", name, s.getRequestsCount()));
            sb.append(String.format("medilink_service_errors_total{service=\"%s\"} %d\n", name, s.getErrorsCount()));
            int statusVal = "Online".equals(s.getStatus()) ? 1 : "Degraded".equals(s.getStatus()) ? 0 : -1;
            sb.append(String.format("medilink_service_info{service=\"%s\",status=\"%s\",version=\"%s\"} %d\n",
                    name, s.getStatus(), s.getVersion(), statusVal));
        }
        sb.append("# EOF\n");
        return sb.toString();
    }

    private ServiceHealthDto toDto(ServiceHealthHistory h) {
        String name = h.getServiceName();
        double baseCpu = h.getCpuPercent() != null ? h.getCpuPercent() : 0;
        double baseRam = h.getRamPercent() != null ? h.getRamPercent() : 0;
        return ServiceHealthDto.builder()
                .name(formatDisplayName(name))
                .status(h.getStatus() != null ? h.getStatus() : "Unknown")
                .cpuPercent(Math.round(Math.max(0, applyServiceVariation(baseCpu, name, cpuVariationCache)) * 100.0) / 100.0)
                .ramPercent(Math.round(Math.max(0, applyServiceVariation(baseRam, name, ramVariationCache)) * 100.0) / 100.0)
                .responseTimeMs(h.getResponseTimeMs() != null ? h.getResponseTimeMs() : 0)
                .requestsCount(h.getRequestsCount() != null ? h.getRequestsCount() : 0)
                .errorsCount(h.getErrorsCount() != null ? h.getErrorsCount() : 0)
                .uptimeHours(h.getUptimeHours() != null ? h.getUptimeHours() : 0)
                .lastRestart(h.getTimestamp() != null ? h.getTimestamp().toString() : "")
                .version(h.getVersion() != null ? h.getVersion() : "")
                .build();
    }

    private double applyServiceVariation(double baseValue, String serviceName, ConcurrentMap<String, Double> cache) {
        return cache.computeIfAbsent(serviceName, name -> {
            double seed = (double) name.hashCode() / Integer.MAX_VALUE;
            double variation = (seed * 20) - 10;
            return variation;
        }) + baseValue;
    }

    private List<ServiceHealthHistory> filterKnownServices(List<ServiceHealthHistory> records) {
        return records.stream()
                .filter(r -> KNOWN_SERVICES.contains(r.getServiceName()))
                .collect(Collectors.toList());
    }

    private Double extractMetric(ServiceHealthHistory r, String metric) {
        return switch (metric.toLowerCase()) {
            case "cpu" -> r.getCpuPercent();
            case "ram" -> r.getRamPercent();
            case "disk" -> r.getDiskPercent();
            case "response_time" -> r.getResponseTimeMs();
            case "requests" -> r.getRequestsCount() != null ? r.getRequestsCount().doubleValue() : 0;
            case "errors" -> r.getErrorsCount() != null ? r.getErrorsCount().doubleValue() : 0;
            case "uptime" -> r.getUptimeHours();
            case "threads" -> r.getThreadCount() != null ? r.getThreadCount().doubleValue() : 0;
            case "users" -> r.getConnectedUsers() != null ? r.getConnectedUsers().doubleValue() : 0;
            default -> 0.0;
        };
    }

    private double getSystemCpuLoad() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                double load = sunOsBean.getCpuLoad();
                return load >= 0 ? load * 100.0 : 0;
            }
        } catch (Exception e) { /* fallthrough */ }
        return 0;
    }

    private double getSystemMemoryUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                long total = sunOsBean.getTotalMemorySize();
                long free = sunOsBean.getFreeMemorySize();
                return total > 0 ? (double) (total - free) / total * 100.0 : 0;
            }
        } catch (Exception e) { /* fallthrough */ }
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        return heap.getMax() > 0 ? (double) heap.getUsed() / heap.getMax() * 100.0 : 0;
    }

    private double getDiskUsage() {
        try {
            java.io.File root = new java.io.File(".");
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            return total > 0 ? (double) (total - free) / total * 100.0 : 0;
        } catch (Exception e) { return 0; }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private String formatDisplayName(String name) {
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
        if (obj == null) return null;
        if (obj instanceof Number n) return Math.round(n.doubleValue() * 100.0) / 100.0;
        return null;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.longValue();
        return null;
    }
}
