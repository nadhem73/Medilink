package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.LogEntryDto;
import com.medilinktunisia.monitoring.dto.LogPageDto;
import com.medilinktunisia.monitoring.entity.ServiceHealthHistory;
import com.medilinktunisia.monitoring.repository.ServiceHealthHistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogService {

    private final ServiceHealthHistoryRepository repository;
    private final List<LogEntryDto> recentLogs = new CopyOnWriteArrayList<>();
    private final AtomicInteger logCounter = new AtomicInteger(0);

    public LogService(ServiceHealthHistoryRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        log.info("Log service initialized — reads historical data from PostgreSQL");
    }

    public void addLog(String service, String level, String message) {
        addLog(service, level, message, "system");
    }

    public void addLog(String service, String level, String message, String user) {
        String id = "LOG-" + String.format("%04d", logCounter.incrementAndGet());
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        LogEntryDto entry = LogEntryDto.builder()
                .id(id)
                .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .time(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .service(service)
                .level(level)
                .message(message)
                .user(user)
                .ipAddress("system")
                .traceId("")
                .build();

        recentLogs.addFirst(entry);

        if (recentLogs.size() > 2000) {
            Object[] snapshot = recentLogs.toArray();
            recentLogs.clear();
            for (int i = 0; i < Math.min(1000, snapshot.length); i++) {
                recentLogs.add((LogEntryDto) snapshot[i]);
            }
        }
    }

    public LogPageDto getLogs(String service, String level, String search,
                              String dateFrom, String dateTo, int page, int pageSize) {
        LocalDateTime from = dateFrom != null && !dateFrom.isEmpty()
                ? LocalDate.parse(dateFrom).atStartOfDay()
                : LocalDateTime.now().minusDays(7);
        LocalDateTime to = dateTo != null && !dateTo.isEmpty()
                ? LocalDate.parse(dateTo).plusDays(1).atStartOfDay()
                : LocalDateTime.now();

        List<ServiceHealthHistory> dbRecords = repository.findByTimestampBetweenOrderByTimestampAsc(from, to);

        List<LogEntryDto> dbLogs = dbRecords.stream().map(this::toLogEntry).collect(Collectors.toList());

        List<LogEntryDto> combined = new ArrayList<>(dbLogs);
        for (LogEntryDto rl : recentLogs) {
            if (rl.getDate().compareTo(from.format(DateTimeFormatter.ISO_LOCAL_DATE)) >= 0 &&
                rl.getDate().compareTo(to.format(DateTimeFormatter.ISO_LOCAL_DATE)) <= 0) {
                combined.add(rl);
            }
        }

        combined.sort((a, b) -> (b.getDate() + b.getTime()).compareTo(a.getDate() + a.getTime()));

        List<LogEntryDto> filtered = combined.stream()
                .filter(l -> service == null || service.isEmpty() || l.getService().equalsIgnoreCase(service))
                .filter(l -> level == null || level.isEmpty() || l.getLevel().equalsIgnoreCase(level))
                .filter(l -> search == null || search.isEmpty() ||
                        l.getMessage().toLowerCase().contains(search.toLowerCase()) ||
                        l.getUser().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        int total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int start = page * pageSize;
        int end = Math.min(start + pageSize, total);

        List<LogEntryDto> pageContent = start < total ? filtered.subList(start, end) : List.of();

        return LogPageDto.builder()
                .logs(pageContent)
                .totalCount(total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }

    public LogEntryDto getLogById(String id) {
        for (LogEntryDto l : recentLogs) {
            if (l.getId().equals(id)) return l;
        }
        return null;
    }

    private LogEntryDto toLogEntry(ServiceHealthHistory h) {
        String level;
        String message;
        if ("Offline".equals(h.getStatus())) {
            level = "ERROR";
            message = String.format("Health check failed — service %s is unreachable", formatSnakeName(h.getServiceName()));
        } else if ("Degraded".equals(h.getStatus())) {
            level = "WARNING";
            message = String.format("Health check degraded — %s (CPU: %.1f%%, RAM: %.1f%%)",
                    formatSnakeName(h.getServiceName()),
                    h.getCpuPercent() != null ? h.getCpuPercent() : 0,
                    h.getRamPercent() != null ? h.getRamPercent() : 0);
        } else {
            level = "INFO";
            message = String.format("Health check OK — %s (CPU: %.1f%%, RAM: %.1f%%, Disk: %.1f%%, %dms)",
                    formatSnakeName(h.getServiceName()),
                    h.getCpuPercent() != null ? h.getCpuPercent() : 0,
                    h.getRamPercent() != null ? h.getRamPercent() : 0,
                    h.getDiskPercent() != null ? h.getDiskPercent() : 0,
                    h.getResponseTimeMs() != null ? h.getResponseTimeMs().longValue() : 0);
        }

        String id = "LOG-" + String.format("%04d", logCounter.incrementAndGet());
        LocalDateTime ts = h.getTimestamp() != null ? h.getTimestamp() : LocalDateTime.now();

        return LogEntryDto.builder()
                .id(id)
                .date(ts.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .time(ts.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .service(formatSnakeName(h.getServiceName()))
                .level(level)
                .message(message)
                .user("system")
                .ipAddress("system")
                .traceId("")
                .build();
    }

    private String formatSnakeName(String name) {
        if (name == null) return "Unknown";
        return switch (name) {
            case "auth-service" -> "Authentication Service";
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
}