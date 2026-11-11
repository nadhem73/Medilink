package com.medilinktunisia.monitoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interroge chaque microservice (health + actuator/metrics) et enregistre
 * une mesure réelle par service. Aucune valeur n'est simulée : si une
 * métrique est indisponible, elle est enregistrée à null.
 */
@Service
@Slf4j
public class MetricsAggregatorService {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    /** service -> URL de base actuator (null = pas d'actuator, health seulement). */
    private static final Map<String, String> ACTUATOR_BASE = new LinkedHashMap<>();
    /** service -> URL du health check. */
    private static final Map<String, String> HEALTH_URL = new LinkedHashMap<>();

    static {
        register("api-gateway", "http://localhost:8765/actuator");
        register("auth-service", "http://localhost:8084/actuator");
        register("patient-service", "http://localhost:8082/actuator");
        register("doctor-service", "http://localhost:8083/actuator");
        register("pharmacy-service", "http://localhost:8085/api/pharmacy/actuator");
        register("prescription-service", "http://localhost:8086/actuator");
        register("bilan-service", "http://localhost:8087/actuator");
        // ai-service (FastAPI) : pas d'actuator, uniquement /health
        ACTUATOR_BASE.put("ai-service", null);
        HEALTH_URL.put("ai-service", "http://localhost:8093/health");
    }

    private static void register(String name, String actuatorBase) {
        ACTUATOR_BASE.put(name, actuatorBase);
        HEALTH_URL.put(name, actuatorBase + "/health");
    }

    private final WebClient webClient;
    private final MetricsPersistenceService persistenceService;
    private final ObjectMapper mapper = new ObjectMapper();

    public MetricsAggregatorService(WebClient.Builder webClientBuilder,
                                    MetricsPersistenceService persistenceService) {
        this.webClient = webClientBuilder.build();
        this.persistenceService = persistenceService;
    }

    public void pollAllServices() {
        for (String name : HEALTH_URL.keySet()) {
            try {
                persistenceService.saveSnapshot(sample(name));
            } catch (Exception e) {
                log.error("Could not record sample for {}: {}", name, e.getMessage());
            }
        }
    }

    private ServiceSample sample(String name) {
        long start = System.nanoTime();
        String status;
        Double responseMs = null;
        try {
            String body = webClient.get().uri(HEALTH_URL.get(name))
                    .retrieve().bodyToMono(String.class).block(TIMEOUT);
            responseMs = round2((System.nanoTime() - start) / 1_000_000.0);
            status = isHealthyBody(body) ? "Online" : "Degraded";
        } catch (WebClientResponseException e) {
            // Le service répond mais signale un problème (ex. 503 DOWN)
            responseMs = round2((System.nanoTime() - start) / 1_000_000.0);
            status = "Degraded";
            log.warn("Health DEGRADED | {} | HTTP {}", name, e.getStatusCode().value());
        } catch (Exception e) {
            status = "Offline";
            log.warn("Health FAIL | {} | {}", name, e.getMessage());
        }

        ServiceSample.ServiceSampleBuilder b = ServiceSample.builder()
                .serviceName(name).status(status).responseTimeMs(responseMs);

        String base = ACTUATOR_BASE.get(name);
        if (base == null || "Offline".equals(status)) {
            return b.build();
        }

        Double cpu = metric(base, "process.cpu.usage", null, "VALUE");
        Double heapUsed = metric(base, "jvm.memory.used", "area:heap", "VALUE");
        Double heapMax = metric(base, "jvm.memory.max", "area:heap", "VALUE");
        Double diskTotal = metric(base, "disk.total", null, "VALUE");
        Double diskFree = metric(base, "disk.free", null, "VALUE");
        Double requests = metric(base, "http.server.requests", null, "COUNT");
        Double errors = metric(base, "http.server.requests", "outcome:SERVER_ERROR", "COUNT");
        Double uptimeSec = metric(base, "process.uptime", null, "VALUE");
        Double threads = metric(base, "jvm.threads.live", null, "VALUE");
        Double startEpoch = metric(base, "process.start.time", null, "VALUE");

        b.cpuPercent(cpu != null ? round2(cpu * 100.0) : null);
        b.ramPercent(heapUsed != null && heapMax != null && heapMax > 0 ? round2(heapUsed / heapMax * 100.0) : null);
        b.diskPercent(diskTotal != null && diskFree != null && diskTotal > 0
                ? round2((diskTotal - diskFree) / diskTotal * 100.0) : null);
        b.requestsCount(requests != null ? requests.longValue() : 0L);
        // Pas de requête en erreur 5xx => le tag n'existe pas encore => 0
        b.errorsCount(errors != null ? errors.longValue() : 0L);
        b.uptimeHours(uptimeSec != null ? round2(uptimeSec / 3600.0) : null);
        b.threadCount(threads != null ? threads.intValue() : null);
        if (startEpoch != null) {
            b.startedAt(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli((long) (startEpoch * 1000)), ZoneId.systemDefault()));
        }
        b.version(buildVersion(base));

        if ("prescription-service".equals(name)) {
            Double notif = metric(base, "medilink.notifications.sent", null, "COUNT");
            b.notificationsCount(notif != null ? notif.longValue() : 0L);
        }
        return b.build();
    }

    /** Lit une statistique d'une métrique actuator ; null si absente ou inaccessible. */
    private Double metric(String base, String metric, String tag, String statistic) {
        try {
            String url = base + "/metrics/" + metric + (tag != null ? "?tag=" + tag : "");
            String body = webClient.get().uri(url).retrieve().bodyToMono(String.class).block(TIMEOUT);
            if (body == null) return null;
            for (JsonNode m : mapper.readTree(body).path("measurements")) {
                if (statistic.equals(m.path("statistic").asText())) {
                    return m.path("value").asDouble();
                }
            }
        } catch (Exception e) {
            log.debug("Metric {} unavailable at {}: {}", metric, base, e.getMessage());
        }
        return null;
    }

    private String buildVersion(String base) {
        try {
            String body = webClient.get().uri(base + "/info").retrieve().bodyToMono(String.class).block(TIMEOUT);
            if (body != null) {
                String v = mapper.readTree(body).path("build").path("version").asText("");
                if (!v.isBlank()) return v.length() > 20 ? v.substring(0, 20) : v;
            }
        } catch (Exception e) { /* version optionnelle */ }
        return null;
    }

    private boolean isHealthyBody(String body) {
        if (body == null) return true;
        try {
            String s = mapper.readTree(body).path("status").asText("");
            return s.isEmpty() || s.equalsIgnoreCase("UP") || s.equalsIgnoreCase("ok") || s.equalsIgnoreCase("healthy");
        } catch (Exception e) {
            return true; // réponse 2xx non JSON : on considère le service en ligne
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
