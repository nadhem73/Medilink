package com.medilinktunisia.monitoring.service;

import com.medilinktunisia.monitoring.dto.AlertDto;
import com.medilinktunisia.monitoring.dto.SuspiciousIpDto;
import com.medilinktunisia.monitoring.entity.ServiceHealthHistory;
import com.medilinktunisia.monitoring.repository.ServiceHealthHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Alertes calculées à partir des dernières mesures réelles de chaque service
 * et des événements de sécurité. Une alerte ouverte passe automatiquement à
 * RESOLVED quand la condition qui l'a déclenchée disparaît.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private static final Set<String> KNOWN_SERVICES = Set.of(
        "auth-service", "patient-service", "doctor-service", "pharmacy-service",
        "prescription-service", "bilan-service", "ai-service", "api-gateway"
    );

    private final ServiceHealthHistoryRepository repository;
    private final PlatformStatsService platformStats;

    private final List<AlertDto> alerts = new CopyOnWriteArrayList<>();
    /** clé de condition -> id de l'alerte active (OPEN ou ACKNOWLEDGED). */
    private final Map<String, String> activeByKey = new LinkedHashMap<>();
    private final AtomicInteger alertCounter = new AtomicInteger(0);

    private record Condition(String key, String title, String description, String priority, String service) {}

    public synchronized void evaluateThresholds() {
        List<Condition> current = new ArrayList<>();

        List<ServiceHealthHistory> latest = repository.findLatestPerService().stream()
                .filter(s -> KNOWN_SERVICES.contains(s.getServiceName()))
                .collect(Collectors.toList());

        for (ServiceHealthHistory s : latest) {
            String name = MetricsPersistenceService.formatDisplayName(s.getServiceName());
            if ("Offline".equals(s.getStatus())) {
                current.add(new Condition("offline:" + s.getServiceName(), name + " hors ligne",
                        name + " ne répond plus au health check.", "CRITICAL", name));
                continue;
            }
            if ("Degraded".equals(s.getStatus())) {
                current.add(new Condition("degraded:" + s.getServiceName(), name + " dégradé",
                        name + " répond mais signale un état non sain.", "HIGH", name));
            }
            threshold(current, "cpu", s.getServiceName(), name, "CPU", s.getCpuPercent(), 80, 90);
            threshold(current, "heap", s.getServiceName(), name, "Mémoire JVM", s.getRamPercent(), 85, 95);
            threshold(current, "disk", "host", "Serveur", "Disque", s.getDiskPercent(), 85, 90);
        }

        for (SuspiciousIpDto ip : platformStats.securityStats().getSuspiciousIps()) {
            current.add(new Condition("ip:" + ip.getIp(), "IP suspecte : " + ip.getIp(),
                    ip.getAttempts() + " échecs de connexion en 24 h (dernier : " + ip.getLastAttempt() + ").",
                    ip.getAttempts() >= 20 ? "CRITICAL" : "HIGH", "Auth Service"));
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Set<String> currentKeys = new HashSet<>();
        for (Condition c : current) {
            if (!currentKeys.add(c.key())) continue; // ex. disque partagé : une seule alerte
            if (!activeByKey.containsKey(c.key())) {
                String id = "ALT-" + String.format("%04d", alertCounter.incrementAndGet());
                alerts.add(0, AlertDto.builder()
                        .id(id).title(c.title()).description(c.description())
                        .priority(c.priority()).date(now).status("OPEN").service(c.service())
                        .build());
                activeByKey.put(c.key(), id);
                log.warn("ALERT [{}] {}: {}", c.priority(), c.title(), c.description());
            }
        }

        // Conditions disparues : alerte résolue automatiquement
        for (Iterator<Map.Entry<String, String>> it = activeByKey.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> e = it.next();
            if (!currentKeys.contains(e.getKey())) {
                alerts.stream().filter(a -> a.getId().equals(e.getValue())).findFirst()
                        .ifPresent(a -> a.setStatus("RESOLVED"));
                it.remove();
            }
        }

        // Historique borné : on retire les plus anciennes alertes résolues
        for (int i = alerts.size() - 1; i >= 0 && alerts.size() > 200; i--) {
            if ("RESOLVED".equals(alerts.get(i).getStatus())) alerts.remove(i);
        }
    }

    private static void threshold(List<Condition> out, String kind, String key, String name, String label,
                                  Double value, double high, double critical) {
        if (value == null || value < high) return;
        String priority = value >= critical ? "CRITICAL" : "HIGH";
        out.add(new Condition(kind + ":" + key, label + " élevé — " + name,
                String.format(Locale.ROOT, "%s à %.1f%% (seuil %.0f%%).", label, value, value >= critical ? critical : high),
                priority, name));
    }

    public List<AlertDto> getAlerts(String priority, String status) {
        return alerts.stream()
                .filter(a -> priority == null || priority.isEmpty() || a.getPriority().equalsIgnoreCase(priority))
                .filter(a -> status == null || status.isEmpty() || a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public AlertDto acknowledgeAlert(String id) {
        for (AlertDto alert : alerts) {
            if (alert.getId().equals(id) && "OPEN".equals(alert.getStatus())) {
                alert.setStatus("ACKNOWLEDGED");
                log.info("Alert {} acknowledged", id);
                return alert;
            }
        }
        return null;
    }
}
