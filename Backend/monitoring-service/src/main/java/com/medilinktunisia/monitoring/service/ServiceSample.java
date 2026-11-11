package com.medilinktunisia.monitoring.service;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Mesure réelle d'un service à un instant donné, lue sur son endpoint
 * /actuator/health + /actuator/metrics. Les champs sont null quand la
 * métrique n'est pas disponible (service hors ligne ou non-Java).
 */
@Value
@Builder
public class ServiceSample {
    String serviceName;
    String status;              // Online | Degraded | Offline
    Double responseTimeMs;      // latence du health check
    Double cpuPercent;          // process.cpu.usage
    Double ramPercent;          // jvm.memory.used / jvm.memory.max (heap)
    Double diskPercent;         // disk.total / disk.free
    Long requestsCount;         // http.server.requests (cumul depuis le démarrage)
    Long errorsCount;           // http.server.requests outcome=SERVER_ERROR (cumul)
    Double uptimeHours;         // process.uptime
    Integer threadCount;        // jvm.threads.live
    LocalDateTime startedAt;    // process.start.time
    String version;             // /actuator/info build.version
    Long notificationsCount;    // medilink.notifications.sent (prescription-service)
}
