package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class MetricsAggregatorService {

    private final WebClient webClient;
    private final MetricsPersistenceService persistenceService;
    private final LogService logService;

    private final List<String> serviceNames = List.of(
        "auth-service", "patient-service", "doctor-service", "pharmacy-service",
        "prescription-service", "bilan-service",
        "ai-service", "api-gateway"
    );

    private final ConcurrentMap<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    public MetricsAggregatorService(WebClient.Builder webClientBuilder,
                                     MetricsPersistenceService persistenceService,
                                     LogService logService) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8765").build();
        this.persistenceService = persistenceService;
        this.logService = logService;
    }

    @PostConstruct
    public void init() {
        for (String name : serviceNames) {
            requestCounts.put(name, new AtomicLong(0));
            errorCounts.put(name, new AtomicLong(0));
        }
        log.info("MetricsAggregatorService initialized with PostgreSQL persistence");
    }

    public void pollAllServices() {
        for (String name : serviceNames) {
            long callStart = System.currentTimeMillis();
            String healthUrl = buildHealthUrl(name);

            try {
                webClient.get()
                        .uri(healthUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribe(
                                response -> {
                                    long elapsed = System.currentTimeMillis() - callStart;
                                    long reqCount = requestCounts.get(name).incrementAndGet();
                                    String status = "Online";
                                    persistenceService.saveSnapshot(name, status, elapsed, reqCount,
                                            errorCounts.get(name).get());
                                    String msg = String.format("Health check OK (%dms)", elapsed);
                                    logService.addLog(formatServiceName(name), "INFO", msg);
                                    log.info("Health OK | {} | {}ms", name, elapsed);
                                },
                                error -> {
                                    long errCount = errorCounts.get(name).incrementAndGet();
                                    persistenceService.saveSnapshot(name, "Offline", 0,
                                            requestCounts.get(name).get(), errCount);
                                    String msg = "Health check failed: " + error.getMessage();
                                    logService.addLog(formatServiceName(name), "ERROR", msg);
                                    log.warn("Health FAIL | {} | {}", name, error.getMessage());
                                }
                        );
            } catch (Exception e) {
                long errCount = errorCounts.get(name).incrementAndGet();
                persistenceService.saveSnapshot(name, "Degraded", 0,
                        requestCounts.get(name).get(), errCount);
                String msg = "Health check error: " + e.getMessage();
                logService.addLog(formatServiceName(name), "CRITICAL", msg);
                log.error("Health ERR | {} | {}", name, e.getMessage());
            }
        }
    }

    private String buildHealthUrl(String name) {
        return switch (name) {
            case "api-gateway" -> "http://localhost:8765/actuator/health";
            case "auth-service" -> "http://localhost:8084/actuator/health";
            case "patient-service" -> "http://localhost:8082/actuator/health";
            case "doctor-service" -> "http://localhost:8083/actuator/health";
            case "pharmacy-service" -> "http://localhost:8085/api/pharmacy/actuator/health";
            case "prescription-service" -> "http://localhost:8086/actuator/health";
            case "bilan-service" -> "http://localhost:8087/actuator/health";
            case "ai-service" -> "http://localhost:8093/health";
            default -> "http://localhost:8765/api/" + name.replace("-service", "") + "/actuator/health";
        };
    }

    private String formatServiceName(String name) {
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
