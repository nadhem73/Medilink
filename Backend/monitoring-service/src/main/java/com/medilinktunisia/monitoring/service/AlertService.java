package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.AlertDto;
import com.medilinktunisia.monitoring.dto.ServiceHealthDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlertService {

    private final List<AlertDto> alerts = new CopyOnWriteArrayList<>();
    private final AtomicInteger alertCounter = new AtomicInteger(0);
    private final MetricsPersistenceService persistenceService;

    private double lastCpuThreshold = 0;
    private double lastRamThreshold = 0;

    public AlertService(MetricsPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @PostConstruct
    public void init() {
        evaluateThresholds();
        log.info("Alert service initialized with real threshold-based evaluation");
    }

    public void evaluateThresholds() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        double cpuLoad = getSystemCpuLoad();
        double ramUsed = getSystemMemoryUsage();
        double diskUsed = getDiskUsage();
        long offlineCount = persistenceService.getLatestServices().stream()
                .filter(s -> "Offline".equals(s.getStatus()) || "Degraded".equals(s.getStatus()))
                .count();

        if (cpuLoad > 90 && lastCpuThreshold < 90) {
            addAlert("CPU > 90%", String.format("System CPU usage reached %.1f%%", cpuLoad), "CRITICAL", "Monitoring Service", now);
        } else if (cpuLoad > 80 && lastCpuThreshold < 80) {
            addAlert("CPU > 80%", String.format("System CPU usage at %.1f%%", cpuLoad), "HIGH", "Monitoring Service", now);
        }

        if (ramUsed > 90 && lastRamThreshold < 90) {
            addAlert("RAM > 90%", String.format("System RAM usage at %.1f%%", ramUsed), "CRITICAL", "Monitoring Service", now);
        } else if (ramUsed > 85 && lastRamThreshold < 85) {
            addAlert("RAM > 85%", String.format("System RAM usage at %.1f%%", ramUsed), "HIGH", "Monitoring Service", now);
        }

        if (diskUsed > 90) {
            addAlert("Disk > 90%", String.format("Disk space usage at %.1f%%", diskUsed), "CRITICAL", "Monitoring Service", now);
        } else if (diskUsed > 85) {
            addAlert("Disk > 85%", String.format("Disk space usage at %.1f%%", diskUsed), "HIGH", "Monitoring Service", now);
        }

        if (offlineCount > 3) {
            addAlert("Multiple services offline", offlineCount + " services are currently unreachable", "CRITICAL", "API Gateway", now);
        } else if (offlineCount > 0) {
            addAlert("Service degradation detected", offlineCount + " service(s) are offline or degraded", "HIGH", "API Gateway", now);
        }

        lastCpuThreshold = cpuLoad;
        lastRamThreshold = ramUsed;
    }

    private void addAlert(String title, String description, String priority, String service, String date) {
        String id = "ALT-" + String.format("%04d", alertCounter.incrementAndGet());
        Optional<AlertDto> existing = alerts.stream()
                .filter(a -> a.getTitle().equals(title) && "OPEN".equals(a.getStatus()))
                .findFirst();
        if (existing.isPresent()) return;

        alerts.add(AlertDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .priority(priority)
                .date(date)
                .status("OPEN")
                .service(service)
                .build());

        if (alerts.size() > 100) {
            Object[] snapshot = alerts.toArray();
            alerts.clear();
            for (int i = 0; i < Math.min(80, snapshot.length); i++) {
                alerts.add((AlertDto) snapshot[i]);
            }
        }
        log.warn("ALERT [{}] {}: {}", priority, title, description);
    }

    public List<AlertDto> getAlerts(String priority, String status) {
        evaluateThresholds();
        return alerts.stream()
                .filter(a -> priority == null || priority.isEmpty() || a.getPriority().equalsIgnoreCase(priority))
                .filter(a -> status == null || status.isEmpty() || a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public AlertDto acknowledgeAlert(String id) {
        for (AlertDto alert : alerts) {
            if (alert.getId().equals(id)) {
                alert.setStatus("ACKNOWLEDGED");
                log.info("Alert {} acknowledged", id);
                return alert;
            }
        }
        return null;
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
            File root = new File(".");
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            return total > 0 ? (double) (total - free) / total * 100.0 : 0;
        } catch (Exception e) { return 0; }
    }
}
